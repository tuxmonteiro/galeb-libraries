/*
 * Copyright (c) 2014-2015 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.core.model;

import io.galeb.core.controller.BackendController;
import io.galeb.core.controller.BackendPoolController;
import io.galeb.core.controller.EntityController;
import io.galeb.core.controller.FarmController;
import io.galeb.core.controller.RuleController;
import io.galeb.core.controller.VirtualHostController;
import io.galeb.core.model.collections.BackendCollection;
import io.galeb.core.model.collections.BackendPoolCollection;
import io.galeb.core.model.collections.Collection;
import io.galeb.core.model.collections.NullEntityCollection;
import io.galeb.core.model.collections.RuleCollection;
import io.galeb.core.model.collections.VirtualHostCollection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.inject.Alternative;

import com.google.gson.annotations.Expose;

@Alternative
public class Farm extends Entity {

    private static final long serialVersionUID = 1L;

    public static final String CLASS_NAME = "Farm";

    @Expose private final Collection<VirtualHost, Rule> virtualHosts = new VirtualHostCollection();
    @Expose private final Collection<BackendPool, Backend> backendPools = new BackendPoolCollection();
    private final Collection<Backend, BackendPool> backends = new BackendCollection();
    private final Collection<Rule, VirtualHost> rules = new RuleCollection();

    private final VirtualHostController virtualHostController = new VirtualHostController(this);
    private final BackendController backendController = new BackendController(this);
    private final BackendPoolController backendPoolController = new BackendPoolController(this);
    private final RuleController ruleController = new RuleController(this);
    private final FarmController farmController = new FarmController(this)
                                                    .setVirtualHostController(virtualHostController)
                                                    .setBackendController(backendController)
                                                    .setBackendPoolController(backendPoolController)
                                                    .setRuleController(ruleController);

    private final Map<String, String> options = new ConcurrentHashMap<>();

    public Farm() {
        setEntityType(Farm.class.getSimpleName().toLowerCase());

        virtualHosts.defineSetOfRelatives(rules);
        backendPools.defineSetOfRelatives(backends);
        backends.defineSetOfRelatives(backendPools);
        rules.defineSetOfRelatives(virtualHosts);

    }

    public Map<String, String> getOptions() {
        return options;
    }

    public Farm setOptions(Map<String, String> options) {
        this.options.putAll(options);
        return this;
    }

    public static String getClassNameFromEntityType(String entityType) {
        switch (entityType) {
            case "virtualhost":
                return VirtualHost.CLASS_NAME;
            case "backendpool":
                return BackendPool.CLASS_NAME;
            case "backend":
                return Backend.CLASS_NAME;
            case "rule":
                return Rule.CLASS_NAME;
            case "farm":
                return Farm.CLASS_NAME;
            default:
                return null;
        }
    }

    public static Class<?> getClassFromEntityType(String entityType) {
        switch (entityType) {
            case "virtualhost":
                return VirtualHost.class;
            case "backendpool":
                return BackendPool.class;
            case "backend":
                return Backend.class;
            case "rule":
                return Rule.class;
            case "farm":
                return Farm.class;
            default:
                return null;
        }
    }

    public EntityController getController(String className) {
        if (className == null) {
            return EntityController.NULL;
        }
        switch (className) {
            case Backend.CLASS_NAME:
                return backendController;
            case BackendPool.CLASS_NAME:
                return backendPoolController;
            case Rule.CLASS_NAME:
                return ruleController;
            case VirtualHost.CLASS_NAME:
                return virtualHostController;
            case Farm.CLASS_NAME:
                return farmController;
            default:
                return EntityController.NULL;
        }
    }

    public Collection<? extends Entity, ? extends Entity> getCollection(Class<? extends Entity> entityClass) {
        switch (entityClass.getSimpleName()) {
            case VirtualHost.CLASS_NAME:
                return virtualHosts;
            case BackendPool.CLASS_NAME:
                return backendPools;
            case Backend.CLASS_NAME:
                return backends;
            case Rule.CLASS_NAME:
                return rules;
            default:
                return new NullEntityCollection();
        }
    }

    public void add(Entity entity) {
        getCollection(entity.getClass()).add(entity);
    }

    public void del(Entity entity) {
        getCollection(entity.getClass()).remove(entity);
    }

    public void change(Entity entity) {
        getCollection(entity.getClass()).change(entity);
    }

    public void clear(Class<? extends Entity> entityClass) {
        getCollection(entityClass).clear();
    }

    public Object getRootHandler() {
        return null;
    }

    public List<Entity> virtualhostsUsingBackend(final String backendId) {
        return getCollection(VirtualHost.class).stream()
            .filter(virtualhost ->
               getCollection(Rule.class).stream()
                   .filter(rule -> rule.getParentId().equals(virtualhost.getId()))
                   .anyMatch(rule ->
                       getCollection(Backend.class).stream()
                       .filter(backend -> backend.getId().equals(backendId))
                       .anyMatch(backend ->
                           backend.getParentId().equals(rule.getProperty(Rule.PROP_TARGET_ID))
                       )
                   )
            )
            .collect(Collectors.toList());
    }
}

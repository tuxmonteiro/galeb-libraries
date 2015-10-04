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

package io.galeb.undertow.loaders;

import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Backend;
import io.galeb.core.model.Backend.Health;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.collections.BackendPoolCollection;
import io.galeb.undertow.handlers.BackendProxyClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BackendLoader implements Loader {

    private final Farm farm;
    private Optional<Logger> optionalLogger = Optional.empty();
    private Map<String, BackendProxyClient> backendPools = new HashMap<>();

    public BackendLoader(final Farm farm) {
        this.farm = farm;
    }

    public Loader setBackendPools(final Map<String, BackendProxyClient> backendPools) {
        this.backendPools = backendPools;
        return this;
    }

    @Override
    public Loader setLogger(final Logger logger) {
        optionalLogger = Optional.ofNullable(logger);
        return this;
    }

    @Override
    public void from(Entity entity, Action action) {
        if (action.equals(Action.DEL_ALL)) {
            farm.getCollection(Backend.class).stream().forEach(backend -> from(backend, Action.DEL));
            return;
        }
        if (hasParent(entity)) {
            final String parentId = entity.getParentId();
            final String backendId = entity.getId();
            final BackendProxyClient backendPool = backendPools.get(parentId);
            boolean isOk = false;

            switch (action) {
                case ADD:
                    final Backend.Health backendHealth = ((Backend) entity).getHealth();
                    if (backendPool!=null) {
                        if (backendHealth==Health.HEALTHY) {
                            backendPool.addHost(newURI(backendId));
                            isOk = true;
                        } else {
                            backendPool.removeHost(newURI(backendId));
                            final String message = "DEL action applied (instead of ADD action) because backend is not "
                                    +Health.HEALTHY.toString()+": "+entity.getId()+" ("+entity.getEntityType()+")";
                            optionalLogger.ifPresent(logger -> logger.debug(message));
                        }
                    }
                    break;

                case DEL:
                    if (backendPool!=null) {
                        backendPool.removeHost(newURI(backendId));
                        isOk = true;
                    }
                    break;

                case CHANGE:
                    from(entity, Action.ADD);
                    isOk = true;
                    break;

                default:
                    break;
            }
            if (isOk) {
                optionalLogger.ifPresent(logger -> logger.debug("Action "+action.toString()+" applied: "+entity.getId()+" ("+entity.getEntityType()+")"));
            }
        }
    }

    @Override
    public void changeIfNecessary(List<Entity> oldEntities, Entity entity) {
        from(entity, Action.CHANGE);
    }

    private boolean hasParent(Entity entity) {
        final String parentId = entity.getParentId();
        return !((BackendPoolCollection)farm.getCollection(BackendPool.class)).getListByID(parentId).isEmpty();
    }

    private URI newURI(String uri) {
        try {
            return new URI(uri);
        } catch (final URISyntaxException e) {
            optionalLogger.ifPresent(logger -> logger.error(e));
        }
        return null;
    }
}

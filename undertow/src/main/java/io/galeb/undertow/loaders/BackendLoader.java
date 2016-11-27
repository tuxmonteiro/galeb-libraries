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
import io.galeb.core.model.Backend;
import io.galeb.core.model.Backend.Health;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.collections.BackendPoolCollection;
import io.galeb.undertow.handlers.BackendProxyClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendLoader implements Loader {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Farm farm;
    private Map<String, BackendProxyClient> backendPools = new HashMap<>();

    public BackendLoader(final Farm farm) {
        this.farm = farm;
    }

    public Loader setBackendPools(final Map<String, BackendProxyClient> backendPools) {
        this.backendPools = backendPools;
        return this;
    }

    @Override
    public synchronized void from(Entity entity, Action action) {
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
                    if (backendPool != null) {
                        final Backend.Health backendHealth = ((Backend) entity).getHealth();
                        if (backendHealth == Health.HEALTHY && !backendPool.contains(URI.create(backendId))) {
                            backendPool.addHost(newURI(backendId));
                            isOk = true;
                        }
                        if (backendHealth == Health.DEAD) {
                            backendPool.removeHost(newURI(backendId));
                            final String message = "DEL action applied (instead of ADD action) because backend is not "
                                    + Health.HEALTHY.toString() + ": " + entity.getId() + " (" + entity.getEntityType() + ")";
                            LOGGER.debug(message);
                        }
                    }
                    break;

                case DEL:
                    if (backendPool != null && backendPool.contains(URI.create(backendId))) {
                        backendPool.removeHost(newURI(backendId));
                        isOk = true;
                    }
                    break;

                case CHANGE:
                    if (backendPool != null) {
                        from(entity, Action.ADD);
                        isOk = true;
                    }
                    break;

                default:
                    break;
            }
            if (isOk) {
                LOGGER.debug("Action "+action.toString()+" applied: "+entity.getId()+" ("+entity.getEntityType()+")");
            }
        }
    }

    @Override
    public synchronized void changeIfNecessary(List<Entity> oldEntities, Entity entity) {
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
            LOGGER.error(e);
        }
        return null;
    }
}

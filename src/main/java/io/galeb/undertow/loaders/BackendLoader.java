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
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.collections.BackendPoolCollection;
import io.galeb.undertow.handlers.BackendProxyClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class BackendLoader implements Loader {

    private final Farm farm;
    private Logger logger;
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
        this.logger = logger;
        return this;
    }

    @Override
    public void from(Entity entity, Action action) {
        if (hasParent(entity)) {
            final String parentId = entity.getParentId();
            final String backendId = entity.getId();
            final BackendProxyClient backendPool = backendPools.get(parentId);

            switch (action) {
                case ADD:
                    final Backend.Health backendHealth = ((Backend) entity).getHealth();
                    if (backendPool!=null) {
                        if (backendHealth==Health.HEALTHY) {
                            backendPool.addHost(newURI(backendId));
                        } else {
                            backendPool.removeHost(newURI(backendId));
                        }
                    }
                    break;

                case DEL:
                    if (backendPool!=null) {
                        backendPool.removeHost(newURI(backendId));
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private boolean hasParent(Entity entity) {
        final String parentId = entity.getParentId();
        return !((BackendPoolCollection)farm.getBackendPools()).getListByID(parentId).isEmpty();
    }

    private URI newURI(String uri) {
        try {
            return new URI(uri);
        } catch (final URISyntaxException e) {
            logger.error(e);
        }
        return null;
    }
}

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
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.util.Constants.SysProp;
import io.galeb.undertow.handlers.BackendProxyClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BackendPoolLoader implements Loader {

    private Optional<Logger> optionalLogger = Optional.empty();
    private Map<String, BackendProxyClient> backendPools = new HashMap<>();
    private Loader backendLoader;
    private final Farm farm;

    public BackendPoolLoader(final Farm farm) {
        this.farm = farm;
    }

    public BackendPoolLoader setBackendPools(final Map<String, BackendProxyClient> backendPools) {
        this.backendPools = backendPools;
        return this;
    }

    public BackendPoolLoader setBackendLoader(final Loader backendLoader) {
        this.backendLoader = backendLoader;
        return this;
    }

    @Override
    public Loader setLogger(final Logger logger) {
        this.optionalLogger = Optional.ofNullable(logger);
        return this;
    }

    @Override
    public void from(Entity entity, Action action) {
        if (action.equals(Action.DEL_ALL)) {
            final Farm farmAsEntity = (Farm) entity;
            farmAsEntity.getCollection(BackendPool.class).stream()
                    .forEach(backendPool -> from(backendPool, Action.DEL));
            return;
        }

        final String backendPoolId = entity.getId();
        BackendProxyClient backendProxyClient;
        boolean isOk = false;

        switch (action) {
            case ADD:
                if (!backendPools.containsKey(backendPoolId)) {
                    final Map<String, Object> properties = new HashMap<>(entity.getProperties());
                    properties.put(BackendPool.class.getSimpleName(), entity.getId());
                    properties.put(Farm.class.getSimpleName(), this.farm);
                    backendProxyClient = new BackendProxyClient().setConnectionsPerThread(maxConnPerThread())
                                                                 .addSessionCookieName("JSESSIONID")
                                                                 .setParams(properties);
                    backendPools.put(backendPoolId, backendProxyClient);
                    isOk = true;
                }
                break;

            case DEL:
                ((BackendPool) entity).getBackends().forEach(b -> backendLoader.from(b, Action.DEL));
                backendPools.remove(backendPoolId);
                isOk = true;
                break;

            case CHANGE:
                if (backendPools.containsKey(backendPoolId)) {
                    backendProxyClient = backendPools.get(entity.getId());
                    final Map<String, Object> params = new HashMap<>(entity.getProperties());
                    params.put(BackendPool.class.getSimpleName(), entity.getId());
                    params.put(Farm.class.getSimpleName(), this.farm);
                    backendProxyClient.setParams(params);
                    backendProxyClient.reset();
                    isOk = true;
                }
                break;

            default:
                optionalLogger.ifPresent(logger -> logger.error(action.toString()+" NOT FOUND"));
        }
        if (isOk) {
            optionalLogger.ifPresent(logger -> logger.debug("Action "+action.toString()+" applied: "+entity.getId()+" ("+entity.getEntityType()+")"));
        }
    }

    private int maxConnPerThread() {
        final String maxConnStr = System.getProperty(SysProp.PROP_MAXCONN.toString(), SysProp.PROP_MAXCONN.def());
        int maxConn = 100;
        if (maxConnStr!=null) {
            try {
                maxConn = Integer.parseInt(maxConnStr);
            } catch (final NumberFormatException ignore) {
            }
        }
        //TODO: get number of IOThreads, instead of the availableProcessors
        return (int)Math.ceil((1.0*maxConn)/Runtime.getRuntime().availableProcessors());
    }

}

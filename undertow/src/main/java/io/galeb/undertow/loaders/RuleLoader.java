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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.core.util.Constants.SysProp;
import io.galeb.undertow.handlers.BackendProxyClient;
import io.galeb.undertow.handlers.PathGlobHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuleLoader implements Loader {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Farm farm;
    private Map<String, BackendProxyClient> backendPools = new HashMap<>();
    private HttpHandler virtualHostHandler = null;

    public RuleLoader(final Farm farm) {
        this.farm = farm;
    }

    public RuleLoader setBackendPools(final Map<String, BackendProxyClient> backendPools) {
        this.backendPools = backendPools;
        return this;
    }

    public RuleLoader setVirtualHostHandler(final HttpHandler virtualHostHandler) {
        this.virtualHostHandler = virtualHostHandler;
        return this;
    }

    @Override
    public synchronized void from(Entity entity, Action action) {
        if (action.equals(Action.DEL_ALL)) {
            farm.getCollection(Rule.class).stream().forEach(rule -> from(rule, Action.DEL));
            return;
        }

        final Rule rule;
        if (entity instanceof Rule) {
            rule = (Rule)entity;
        } else {
            return;
        }

        if (hasParent(rule.getParentId())) {
            final String virtualhostId = entity.getParentId();
            if (virtualhostId == null || rule.getMatch() == null) {
                return;
            }
            final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) virtualHostHandler).getHosts();
            boolean isOk = false;

            switch (action) {
                case ADD:
                    if (hasTarget(rule.getTargetId())) {
                        final int maxRequestTime = 0;

                        if (!Integer.toString(StatusCodes.NOT_FOUND).equals(rule.getTargetId())) {
                            final BackendProxyClient backendPool = backendPools.get(rule.getTargetId());
                            if (backendPool==null) {
                                LOGGER.error("addRule("+entity.getId()+"): TargetId not found");
                                return;
                            }
                            HttpHandler nextHandler = hosts.get(virtualhostId);
                            HttpHandler pathHandler = null;
                            if (nextHandler instanceof PathGlobHandler) {
                                pathHandler = nextHandler;
                            } else if (nextHandler instanceof IPAddressAccessControlHandler) {
                                pathHandler = ((IPAddressAccessControlHandler) nextHandler).getNext();
                            }

                            if (pathHandler instanceof PathGlobHandler && !((PathGlobHandler) pathHandler).contains(rule)) {
                                final HttpHandler targetHandler =
                                        new ProxyHandler(backendPool, maxRequestTime, ResponseCodeHandler.HANDLE_500, false, reuseXForwarded());
                                ((PathGlobHandler) pathHandler).addRule(rule, targetHandler);
                                if (rule.isDefault()) {
                                    ((PathGlobHandler)pathHandler).setDefaultHandler(targetHandler);
                                }
                                isOk = true;
                            }
                        }
                    } else {
                        final String message = "Action ADD not applied - " + entity.getId() +
                                " (" + entity.getEntityType() + "): " +
                                rule.getTargetId() + " NOT FOUND";
                        LOGGER.debug(message);
                    }
                    break;

                case DEL:
                    final HttpHandler pathHandler = hosts.get(virtualhostId);
                    if (pathHandler instanceof PathGlobHandler && ((PathGlobHandler) pathHandler).contains(rule)) {
                        ((PathGlobHandler) pathHandler).removeRule(rule);
                        isOk = true;
                    }
                    break;

                case CHANGE:
                    from(entity, Action.DEL);
                    from(entity, Action.ADD);
                    break;

                default:
                    LOGGER.error(action.toString()+" NOT FOUND");
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

    private boolean hasParent(String parentId) {
        return parentId != null && !farm.getCollection(VirtualHost.class).getListByID(parentId).isEmpty();
    }

    private boolean hasTarget(String targetId) {
        return targetId != null && !farm.getCollection(BackendPool.class).getListByID(targetId).isEmpty();
    }

    private boolean reuseXForwarded() {
        String reuseXForwardedStr = System.getProperty(SysProp.PROP_REUSE_XFORWARDED.toString(),
                SysProp.PROP_REUSE_XFORWARDED.def());
        return Boolean.valueOf(reuseXForwardedStr);
    }

}

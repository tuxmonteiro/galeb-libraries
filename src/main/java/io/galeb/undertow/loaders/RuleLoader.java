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
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.core.model.collections.BackendPoolCollection;
import io.galeb.core.model.collections.VirtualHostCollection;
import io.galeb.undertow.handlers.BackendProxyClient;
import io.galeb.undertow.handlers.PathHolderHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.StatusCodes;

import java.util.HashMap;
import java.util.Map;

public class RuleLoader implements Loader {

    private final Farm farm;
    private Logger logger;
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
    public Loader setLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public void from(Entity entity, Action action) {
        if (action.equals(Action.DEL_ALL)) {
            farm.getCollection(Rule.class).stream().forEach(rule -> from(rule, Action.DEL));
            return;
        }

        if (hasParent(entity)) {
            final String virtualhostId = entity.getParentId();
            final String match = (String)entity.getProperty(Rule.PROP_MATCH);
            final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) virtualHostHandler).getHosts();

            switch (action) {
                case ADD:
                    if (hasTarget((Rule) entity)) {
                        final String targetId = (String)entity.getProperty(Rule.PROP_TARGET_ID);
                        final int maxRequestTime = 0;

                        if (!Integer.toString(StatusCodes.NOT_FOUND).equals(targetId)) {
                            final BackendProxyClient backendPool = backendPools.get(targetId);
                            if (backendPool==null) {
                                logger.error("addRule("+entity.getId()+"): TargetId not found");
                                return;
                            }
                            HttpHandler ruleHandler = hosts.get(virtualhostId);
                            if (!(ruleHandler instanceof PathHolderHandler)) {
                                ruleHandler = new PathHolderHandler(ResponseCodeHandler.HANDLE_404);
                            }
                            final HttpHandler targetHandler = new ProxyHandler(backendPool, maxRequestTime, ResponseCodeHandler.HANDLE_404);
                            ((PathHolderHandler) ruleHandler).addPrefixPath(match, targetHandler);
                            hosts.put(virtualhostId, ruleHandler);
                        }
                    }
                    break;

                case DEL:
                    final HttpHandler ruleHandler = hosts.get(virtualhostId);
                    if (ruleHandler!=null && ruleHandler instanceof PathHandler) {
                        ((PathHandler)ruleHandler).removePrefixPath(match);
                    }
                    break;

                case CHANGE:
                    from(entity, Action.DEL);
                    from(entity, Action.ADD);
                    break;

                default:
                    logger.error(action.toString()+" NOT FOUND");
            }
        }
    }

    private boolean hasParent(Entity entity) {
        final String parentId = entity.getParentId();
        return !((VirtualHostCollection) farm.getCollection(VirtualHost.class)).getListByID(parentId).isEmpty();
    }

    private boolean hasTarget(Rule rule) {
        final String targetId = (String) rule.getProperty(Rule.PROP_TARGET_ID);
        return !((BackendPoolCollection)farm.getCollection(BackendPool.class)).getListByID(targetId).isEmpty();
    }

}

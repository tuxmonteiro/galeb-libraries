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

package io.galeb.undertow.model;

import static io.galeb.core.util.Constants.TRUE;
import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.json.JsonObject;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Backend;
import io.galeb.core.model.Backend.Health;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.core.util.Constants.SysProp;
import io.galeb.undertow.handlers.BackendProxyClient;
import io.galeb.undertow.handlers.BackendSelector;
import io.galeb.undertow.handlers.MonitorHeadersHandler;
import io.galeb.undertow.handlers.PathHolderHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.CopyOnWriteMap;
import io.undertow.util.StatusCodes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

@Default
public class FarmUndertow extends Farm {

    @Inject
    private Logger log;

    @Inject
    private IEventBus eventBus;

    private static final long serialVersionUID = 1L;

    private static final String LOGPATTERN = "%h %l %u %t \"%r\" %s %b (%v -> %{i,"+BackendSelector.X_PROXY_HOST+"} [%D]ms \"X-Real-IP: %{i,X-Real-IP}\" \"X-Forwarded-For: %{i,X-Forwarded-For}\")";

    private final HttpHandler virtualHostHandler = new NameVirtualHostHandler();

    private HttpHandler hostMetricsHandler;

    private HttpHandler rootHandler;

    private final Map<String, BackendProxyClient> backendPoolsUndertow = new CopyOnWriteMap<>();

    private final Map<Class<? extends Entity>, Class<? extends Entity>> parentMap = new HashMap<>();

    public FarmUndertow() {
        super();
        parentMap.put(Backend.class, BackendPool.class);
        parentMap.put(Rule.class, VirtualHost.class);
    }

    @PostConstruct
    public void init() {
        hostMetricsHandler = new MonitorHeadersHandler(virtualHostHandler)
                                                        .setEventBus(eventBus)
                                                        .setLogger(log);
        rootHandler = TRUE.equals(System.getProperty(SysProp.PROP_ENABLE_ACCESSLOG.toString(), SysProp.PROP_ENABLE_ACCESSLOG.def())) ?
                new AccessLogHandler(hostMetricsHandler, new AccessLogReceiver() {

            private final ExtendedLogger logger = LogManager.getContext().getLogger(SysProp.PROP_ENABLE_ACCESSLOG.toString());

            @Override
            public void logMessage(String message) {
                logger.info(message);
            }

        }, LOGPATTERN, FarmUndertow.class.getClassLoader()) : hostMetricsHandler;
    }

    @Override
    public HttpHandler getRootHandler() {
        return rootHandler;
    }

    @Override
    public Farm addBackend(JsonObject jsonObject) {
        super.addBackend(jsonObject);
        final Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        backendToUndertow(backend, Action.ADD);
        return this;
    }

    @Override
    public Farm delBackend(JsonObject jsonObject) {
        super.delBackend(jsonObject);
        final Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        backendToUndertow(backend, Action.DEL);
        return this;
    }

    @Override
    public Farm addBackendPool(JsonObject jsonObject) {
        super.addBackendPool(jsonObject);
        final BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        backendPoolToUndertow(backendPool, Action.ADD);
        return this;
    }

    @Override
    public Farm delBackendPool(JsonObject jsonObject) {
        super.delBackendPool(jsonObject);
        final BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        backendPoolToUndertow(backendPool, Action.DEL);
        return this;
    }

    @Override
    public Farm changeBackendPool(JsonObject jsonObject) {
        final BackendPool backendPool = getBackendPool(jsonObject);
        if (backendPool!=null) {
            super.changeBackendPool(jsonObject);

            final BackendProxyClient backendProxyClient = backendPoolsUndertow.get(backendPool.getId());

            final Map<String, Object> params = new HashMap<>(backendPool.getProperties());
            params.put(BackendPool.class.getSimpleName(), backendPool.getId());
            params.put(Farm.class.getSimpleName(), this);

            backendProxyClient.setParams(params);
            backendProxyClient.reset();
        }
        return this;
    }

    @Override
    public Farm addRule(JsonObject jsonObject) {
        super.addRule(jsonObject);
        final Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        ruleToUndertow(rule, Action.ADD);
        return this;
    }

    @Override
    public Farm delRule(JsonObject jsonObject) {
        final Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        ruleToUndertow(rule, Action.DEL);
        return super.delRule(jsonObject);
    }

    @Override
    public Farm addVirtualHost(JsonObject jsonObject) {
        super.addVirtualHost(jsonObject);
        final VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        virtualHostToUndertow(virtualhost, Action.ADD);
        return this;
    }

    @Override
    public Farm delVirtualHost(JsonObject jsonObject) {
        super.delVirtualHost(jsonObject);
        final VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        virtualHostToUndertow(virtualhost, Action.DEL);
        return this;
    }

    private void backendPoolToUndertow(BackendPool backendPool, Action action) {
        final String backendPoolId = backendPool.getId();

        switch (action) {
            case ADD:
                final Map<String, Object> properties = new HashMap<>(backendPool.getProperties());
                properties.put(BackendPool.class.getSimpleName(), backendPool.getId());
                properties.put(Farm.class.getSimpleName(), this);

                final BackendProxyClient backendProxyClient =
                        new BackendProxyClient().setConnectionsPerThread(maxConnPerThread())
                                                .addSessionCookieName("JSESSIONID")
                                                .setParams(properties);

                processBackend(backendPoolId);

                backendPoolsUndertow.put(backendPoolId, backendProxyClient);
                break;
            case DEL:
                backendPoolsUndertow.remove(backendPoolId);
                break;
            default:
                log.error(action.toString()+" NOT FOUND");
        }

    }

    private void virtualHostToUndertow(VirtualHost virtualhost, Action action) {
        final String virtualhostId = virtualhost.getId();

        switch (action) {
            case ADD:
                ((NameVirtualHostHandler) virtualHostHandler).addHost(virtualhostId, ResponseCodeHandler.HANDLE_404);
                break;
            case DEL:
                ((NameVirtualHostHandler) virtualHostHandler).removeHost(virtualhostId);
                break;
            default:
                log.error(action.toString()+" NOT FOUND");
        }

        processRule(virtualhostId);
    }

    private void ruleToUndertow(Rule rule, Action action) {
        if (hasRequisites(rule)) {
            final String virtualhostId = rule.getParentId();
            final String match = (String)rule.getProperty(Rule.PROP_MATCH);
            final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) virtualHostHandler).getHosts();

            switch (action) {
                case ADD:
                    final String targetId = (String)rule.getProperty(Rule.PROP_TARGET_ID);
                    final int maxRequestTime = 30000;

                    if (!hosts.containsKey(virtualhostId)) {
                        log.error("addRule("+rule.getId()+"): ParentId not found");
                        return;
                    }

                    if (!Integer.toString(StatusCodes.NOT_FOUND).equals(targetId)) {
                        final BackendProxyClient backendPool = backendPoolsUndertow.get(targetId);
                        if (backendPool==null) {
                            log.error("addRule("+rule.getId()+"): TargetId not found");
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
                    break;

                case DEL:
                    final HttpHandler ruleHandler = hosts.get(virtualhostId);
                    if (ruleHandler!=null && ruleHandler instanceof PathHandler) {
                        ((PathHandler)ruleHandler).removePrefixPath(match);
                    }
                    break;

                default:
                    log.error(action.toString()+" NOT FOUND");
            }

        }
    }

    private void backendToUndertow(Backend backend, Action action) {
        if (hasRequisites(backend)) {
            final String parentId = backend.getParentId();
            final String backendId = backend.getId();
            final BackendProxyClient backendPool = backendPoolsUndertow.get(parentId);

            switch (action) {
                case ADD:
                    final Backend.Health backendHealth = backend.getHealth();
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

    private void processBackend(String backendPoolId) {
        getBackends().stream()
                     .filter(b -> b.getParentId().equals(backendPoolId))
                     .forEach(b -> this.backendToUndertow(b, Action.ADD));
    }

    private void processRule(String virtualhostId) {
        getRules().stream()
                  .filter(r -> r.getParentId().equals(virtualhostId))
                  .forEach(r -> this.ruleToUndertow(r, Action.ADD));
    }

    private boolean process() {


        return true;
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

    private boolean hasRequisites(Entity entity) {
        boolean exists = false;
        if (!"".equals(entity.getParentId())) {
            exists = this.contains(parentMap.get(entity.getClass()), entity.getParentId());
        }
        return exists;
    }

    private boolean contains(Class<? extends Entity> parentClass, String parentId) {
        if (parentClass.equals(BackendPool.class)) {
            return getBackendPool(parentId) != null;
        } else if (parentClass.equals(VirtualHost.class)) {
            return getVirtualHost(parentId) != null;
        }
        return false;
    }

    private URI newURI(String uri) {
        try {
            return new URI(uri);
        } catch (final URISyntaxException e) {
            log.error(e);
        }
        return null;
    }

}

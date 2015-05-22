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
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.core.util.Constants.SysProp;
import io.galeb.undertow.handlers.BackendProxyClient;
import io.galeb.undertow.handlers.BackendSelector;
import io.galeb.undertow.handlers.MonitorHeadersHandler;
import io.galeb.undertow.loaders.BackendLoader;
import io.galeb.undertow.loaders.BackendPoolLoader;
import io.galeb.undertow.loaders.Loader;
import io.galeb.undertow.loaders.RuleLoader;
import io.galeb.undertow.loaders.VirtualHostLoader;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.util.CopyOnWriteMap;

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

    private HttpHandler rootHandler;
    private final HttpHandler virtualHostHandler = new NameVirtualHostHandler();

    private Loader backendLoader;
    private Loader ruleLoader;
    private Loader backendPoolLoader;
    private Loader virtualHostLoader;

    public FarmUndertow() {
        super();
    }

    @PostConstruct
    public void init() {
        setRootHandler();
        startLoaders();
    }

    private void setRootHandler() {
        final HttpHandler hostMetricsHandler = new MonitorHeadersHandler(virtualHostHandler)
                                                            .setEventBus(eventBus)
                                                            .setLogger(log);

        String enableAccessLogProperty = System.getProperty(SysProp.PROP_ENABLE_ACCESSLOG.toString(),
                                                            SysProp.PROP_ENABLE_ACCESSLOG.def());

        final String LOGPATTERN = "%h %l %u %t \"%r\" %s %b (%v -> %{i,"+BackendSelector.X_PROXY_HOST+"} [%D]ms \"X-Real-IP: %{i,X-Real-IP}\" \"X-Forwarded-For: %{i,X-Forwarded-For}\")";

        final AccessLogReceiver accessLogReceiver  = new AccessLogReceiver() {
            private final ExtendedLogger logger = LogManager.getContext().getLogger(SysProp.PROP_ENABLE_ACCESSLOG.toString());

            @Override
            public void logMessage(String message) {
                logger.info(message);
            }
        };

        rootHandler = TRUE.equals(enableAccessLogProperty) ?
                new AccessLogHandler(hostMetricsHandler,
                                     accessLogReceiver, LOGPATTERN,
                                     FarmUndertow.class.getClassLoader()) :
                hostMetricsHandler;
    }

    private void startLoaders() {
        final Map<String, BackendProxyClient> backendPoolsUndertow = new CopyOnWriteMap<>();

        backendLoader = new BackendLoader(this)
                                .setBackendPools(backendPoolsUndertow)
                                .setLogger(log);

        backendPoolLoader = new BackendPoolLoader()
                                .setBackendPools(backendPoolsUndertow)
                                .setBackendLoader(backendLoader)
                                .setLogger(log);

        ruleLoader = new RuleLoader(this)
                                .setBackendPools(backendPoolsUndertow)
                                .setVirtualHostHandler(virtualHostHandler)
                                .setLogger(log);

        virtualHostLoader = new VirtualHostLoader()
                                .setRuleLoader(ruleLoader)
                                .setVirtualHostHandler(virtualHostHandler)
                                .setLogger(log);
    }

    @Override
    public HttpHandler getRootHandler() {
        return rootHandler;
    }

    @Override
    public Farm addBackend(JsonObject jsonObject) {
        super.addBackend(jsonObject);
        processAll();
        return this;
    }

    @Override
    public Farm delBackend(JsonObject jsonObject) {
        super.delBackend(jsonObject);
        final Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        backendLoader.from(backend, Action.DEL);
        return this;
    }

    @Override
    public Farm addBackendPool(JsonObject jsonObject) {
        super.addBackendPool(jsonObject);
        processAll();
        return this;
    }

    @Override
    public Farm delBackendPool(JsonObject jsonObject) {
        super.delBackendPool(jsonObject);
        final BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        backendPoolLoader.from(backendPool, Action.DEL);
        return this;
    }

    @Override
    public Farm changeBackendPool(JsonObject jsonObject) {
        super.changeBackendPool(jsonObject);
        final BackendPool backendPool = getBackendPool(jsonObject);
        backendPoolLoader.from(backendPool, Action.CHANGE);
        return this;
    }

    @Override
    public Farm addRule(JsonObject jsonObject) {
        super.addRule(jsonObject);
        processAll();
        return this;
    }

    @Override
    public Farm delRule(JsonObject jsonObject) {
        final Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        ruleLoader.from(rule, Action.DEL);
        return super.delRule(jsonObject);
    }

    @Override
    public Farm addVirtualHost(JsonObject jsonObject) {
        super.addVirtualHost(jsonObject);
        processAll();
        return this;
    }

    @Override
    public Farm delVirtualHost(JsonObject jsonObject) {
        super.delVirtualHost(jsonObject);
        final VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        virtualHostLoader.from(virtualhost, Action.DEL);
        return this;
    }

    private synchronized void processAll() {
        getBackendPools().forEach(backendPool -> {
            backendPoolLoader.from(backendPool, Action.ADD);
            backendPool.getBackends().forEach(backend -> backendLoader.from(backend, Action.ADD));
        });
        getVirtualHosts().forEach(virtualhost -> {
            virtualHostLoader.from(virtualhost, Action.ADD);
            virtualhost.getRules().forEach(rule -> ruleLoader.from(rule, Action.ADD));
        });
    }

}

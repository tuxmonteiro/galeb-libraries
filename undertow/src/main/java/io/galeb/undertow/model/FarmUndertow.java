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

import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.core.statsd.StatsdClient;
import io.galeb.core.util.Constants.SysProp;
import io.galeb.undertow.handlers.AccessLogExtendedHandler;
import io.galeb.undertow.handlers.BackendProxyClient;
import io.galeb.undertow.handlers.MonitorHeadersHandler;
import io.galeb.undertow.loaders.BackendLoader;
import io.galeb.undertow.loaders.BackendPoolLoader;
import io.galeb.undertow.loaders.Loader;
import io.galeb.undertow.loaders.RuleLoader;
import io.galeb.undertow.loaders.VirtualHostLoader;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import io.undertow.util.CopyOnWriteMap;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;

@Default
public class FarmUndertow extends Farm {

    private enum HEALTHCHECK_CONTENT {
        WORKING,
        EMPTY,
        FAIL
    }

    private static final Logger LOGGER = LogManager.getLogger();

    private static final long serialVersionUID = 1L;

    @Inject
    private StatsdClient statsdClient;

    private HttpHandler rootHandler;
    private final NameVirtualHostHandler virtualHostHandler = new NameVirtualHostHandler().addHost("__ping__", healthCheckHandler());

    private HttpHandler healthCheckHandler() {
        return httpServerExchange -> {
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            httpServerExchange.getResponseHeaders().put(Headers.SERVER, "Galeb");
            String healthCheckContent = isFarmFailed() ? HEALTHCHECK_CONTENT.FAIL.toString() :
                                        isVirtualhostsNotEmpty() ? HEALTHCHECK_CONTENT.WORKING.toString() : HEALTHCHECK_CONTENT.EMPTY.toString();
            httpServerExchange.getResponseSender().send(healthCheckContent);
        };
    }

    private final Map<Class<? extends Entity>, Loader> mapOfLoaders = new HashMap<>();

    private int maxRequestTime = Integer.MAX_VALUE - 1;
    private boolean forceChangeStatus = false;

    public FarmUndertow() {
        super();
    }

    @PostConstruct
    public void init() {
        setRootHandler();
        startLoaders();
    }

    private void setRootHandler() {
        this.maxRequestTime = Integer.valueOf(STATIC_PROPERTIES.getOrDefault(MAX_REQUEST_TIME_FARM_PROP, String.valueOf(Integer.MAX_VALUE - 1)));
        this.forceChangeStatus = Boolean.valueOf(STATIC_PROPERTIES.getOrDefault(FORCE_CHANGE_STATUS_FARM_PROP, String.valueOf(false)));

        virtualHostHandler.setDefaultHandler(ResponseCodeHandler.HANDLE_500);
        final HttpHandler hostMetricsHandler =
                new MonitorHeadersHandler(virtualHostHandler).setStatsd(statsdClient)
                                                             .setMaxRequestTime(maxRequestTime)
                                                             .forceChangeStatus(forceChangeStatus);

        final String enableAccessLogProperty = System.getProperty(SysProp.PROP_ENABLE_ACCESSLOG.toString(),
                                                                  SysProp.PROP_ENABLE_ACCESSLOG.def());

        final String LOGPATTERN = "%a\t%v\t%r\t-\t-\tLocal: %s\t*-\t%B\t%D\tProxy: %s\t-\t%b\t-\t"
                + AccessLogExtendedHandler.REAL_DEST + "\tAgent: %{i,User-Agent}\tFwd: %{i,X-Forwarded-For}";

        final AccessLogReceiver accessLogReceiver  = new AccessLogReceiver() {
            private final ExtendedLogger logger =
                    LogManager.getContext().getLogger(SysProp.PROP_ENABLE_ACCESSLOG.toString());

            @Override
            public void logMessage(String message) {
                logger.info(message);
            }
        };

        rootHandler = Boolean.parseBoolean(enableAccessLogProperty) ?
                new AccessLogExtendedHandler(hostMetricsHandler,
                                             accessLogReceiver,
                                             LOGPATTERN,
                                             FarmUndertow.class.getClassLoader())
                        .setMaxRequestTime(maxRequestTime) :
                hostMetricsHandler;
    }

    private void startLoaders() {
        final Map<String, BackendProxyClient> backendPoolsUndertow = new CopyOnWriteMap<>();

        Loader backendLoader;
        Loader ruleLoader;

        mapOfLoaders.put(Backend.class, backendLoader = new BackendLoader(this)
                                                .setBackendPools(backendPoolsUndertow));

        mapOfLoaders.put(BackendPool.class, new BackendPoolLoader(this)
                                                .setBackendPools(backendPoolsUndertow)
                                                .setBackendLoader(backendLoader));

        mapOfLoaders.put(Rule.class, ruleLoader = new RuleLoader(this)
                                                .setBackendPools(backendPoolsUndertow)
                                                .setVirtualHostHandler(virtualHostHandler));

        mapOfLoaders.put(VirtualHost.class, new VirtualHostLoader(this)
                                                .setRuleLoader(ruleLoader)
                                                .setVirtualHostHandler(virtualHostHandler));
    }

    @Override
    public void add(Entity entity) {
        super.add(entity);
    }

    @Override
    public void del(Entity entity) {
        mapOfLoaders.get(entity.getClass()).from(entity, Action.DEL);
        super.del(entity);
    }

    @Override
    public void change(Entity entity) {
        List<Entity> oldEntities = getCollection(entity.getClass()).getListByID(entity.getId());
        super.change(entity);
        mapOfLoaders.get(entity.getClass()).changeIfNecessary(oldEntities, entity);
    }

    @Override
    public void clear(Class<? extends Entity> entityClass) {
        mapOfLoaders.get(entityClass).from(this, Action.DEL_ALL);
        super.clear(entityClass);
    }

    @Override
    public HttpHandler getRootHandler() {
        return rootHandler;
    }

    public synchronized void processAll() {
        getCollection(BackendPool.class).stream().forEach(backendPool -> {
            mapOfLoaders.get(BackendPool.class).from(backendPool, Action.ADD);
            getCollection(Backend.class).stream()
                .filter(backend -> backend.getParentId().equals(backendPool.getId()))
                .forEach(backend -> {
                    ((BackendPool) backendPool).addBackend(backend.getId());
                    mapOfLoaders.get(Backend.class).from(backend, Action.ADD);
                });
        });
        getCollection(VirtualHost.class).stream().forEach(virtualhost -> {
            mapOfLoaders.get(VirtualHost.class).from(virtualhost, Action.ADD);
            getCollection(Rule.class).stream()
                .filter(rule -> rule.getParentId().equals(virtualhost.getId()))
                .forEach(rule -> {
                    ((VirtualHost) virtualhost).addRule(rule.getId());
                    mapOfLoaders.get(Rule.class).from(rule, Action.ADD);
                });
        });
    }
}

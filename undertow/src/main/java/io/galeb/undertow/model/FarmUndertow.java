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
import io.galeb.core.logging.Logger;
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
import io.galeb.undertow.scheduler.*;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.util.CopyOnWriteMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

@Default
public class FarmUndertow extends Farm {

    private static final long serialVersionUID = 1L;

    @Inject
    private Logger log;

    @Inject
    private StatsdClient statsdClient;

    private HttpHandler rootHandler;
    private final HttpHandler virtualHostHandler = new NameVirtualHostHandler();
    private final Map<Class<? extends Entity>, Loader> mapOfLoaders = new HashMap<>();

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
                                                    .setStatsd(statsdClient)
                                                    .setLogger(log);

        final String enableAccessLogProperty = System.getProperty(SysProp.PROP_ENABLE_ACCESSLOG.toString(),
                                                                  SysProp.PROP_ENABLE_ACCESSLOG.def());

        final String EXTENDED_LOGPATTERN = "(%v -> " + AccessLogExtendedHandler.REAL_DEST
                                           + " [%D]ms \"X-Real-IP: %{i,X-Real-IP}\""
                                           + " \"X-Forwarded-For: %{i,X-Forwarded-For}\")";
        final String LOGPATTERN = "%h %l %u %t \"%r\" %s %b " + EXTENDED_LOGPATTERN;

        final AccessLogReceiver accessLogReceiver  = new AccessLogReceiver() {
            private final ExtendedLogger logger =
                    LogManager.getContext().getLogger(SysProp.PROP_ENABLE_ACCESSLOG.toString());

            @Override
            public void logMessage(String message) {
                logger.info(message);
            }
        };

        rootHandler = TRUE.equals(enableAccessLogProperty) ?
                new AccessLogExtendedHandler(hostMetricsHandler,
                                             accessLogReceiver,
                                             LOGPATTERN,
                                             FarmUndertow.class.getClassLoader()) :
                hostMetricsHandler;
    }

    private void startLoaders() {
        final Map<String, BackendProxyClient> backendPoolsUndertow = new CopyOnWriteMap<>();

        Loader backendLoader;
        Loader ruleLoader;

        mapOfLoaders.put(Backend.class, backendLoader = new BackendLoader(this)
                                                .setBackendPools(backendPoolsUndertow)
                                                .setLogger(log));

        mapOfLoaders.put(BackendPool.class, new BackendPoolLoader(this)
                                                .setBackendPools(backendPoolsUndertow)
                                                .setBackendLoader(backendLoader)
                                                .setLogger(log));

        mapOfLoaders.put(Rule.class, ruleLoader = new RuleLoader(this)
                                                .setBackendPools(backendPoolsUndertow)
                                                .setVirtualHostHandler(virtualHostHandler)
                                                .setLogger(log));

        mapOfLoaders.put(VirtualHost.class, new VirtualHostLoader()
                                                .setRuleLoader(ruleLoader)
                                                .setVirtualHostHandler(virtualHostHandler)
                                                .setLogger(log));
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
                    ((BackendPool) backendPool).addBackend((Backend)backend);
                    mapOfLoaders.get(Backend.class).from(backend, Action.ADD);
                });
        });
        getCollection(VirtualHost.class).stream().forEach(virtualhost -> {
            mapOfLoaders.get(VirtualHost.class).from(virtualhost, Action.ADD);
            getCollection(Rule.class).stream()
                .filter(rule -> rule.getParentId().equals(virtualhost.getId()))
                .forEach(rule -> {
                    ((VirtualHost) virtualhost).addRule((Rule)rule);
                    mapOfLoaders.get(Rule.class).from(rule, Action.ADD);
                });
        });
    }
}

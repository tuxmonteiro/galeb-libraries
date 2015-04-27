package io.galeb.undertow.model;

import static io.galeb.core.util.Constants.PROP_ENABLE_ACCESSLOG;
import static io.galeb.core.util.Constants.TRUE;
import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.json.JsonObject;
import io.galeb.core.loadbalance.LoadBalancePolicy;
import io.galeb.core.loadbalance.LoadBalancePolicyLocator;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Backend;
import io.galeb.core.model.Backend.Health;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.undertow.handlers.BackendProxyClient;
import io.galeb.undertow.handlers.MonitorHeadersHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.CopyOnWriteMap;

import java.net.URI;
import java.net.URISyntaxException;
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

    private static final String LOGPATTERN = "%h %l %u %t \"%r\" %s %b (%v -> %{i,X-Proxy-Host} [%D]ms \"X-Real-IP: %{i,X-Real-IP}\" \"X-Forwarded-For: %{i,X-Forwarded-For}\")";

    private final HttpHandler virtualHostHandler = new NameVirtualHostHandler();

    private HttpHandler hostMetricsHandler;

    private HttpHandler rootHandler;

    private final Map<String, BackendProxyClient> backendPoolsUndertow = new CopyOnWriteMap<>();

    public FarmUndertow() {
        super();
    }

    @PostConstruct
    public void init() {
        hostMetricsHandler = new MonitorHeadersHandler(virtualHostHandler).setEventBus(eventBus);
        rootHandler = TRUE.equals(System.getProperty(PROP_ENABLE_ACCESSLOG)) ? new AccessLogHandler(hostMetricsHandler, new AccessLogReceiver() {

            private final ExtendedLogger logger = LogManager.getContext().getLogger(PROP_ENABLE_ACCESSLOG);

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
        final Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        final String parentId = backend.getParentId();
        final String backendId = backend.getId();
        final Backend.Health backendHealth = backend.getHealth();

        final BackendProxyClient backendPool = backendPoolsUndertow.get(parentId);
        if (backendPool!=null) {
            if (backendHealth==Health.HEALTHY) {
                try {
                    backendPool.addHost(new URI(backendId));
                } catch (final URISyntaxException e) {
                    log.error(e);
                }
            } else {
                try {
                    backendPool.removeHost(new URI(backendId));
                } catch (final URISyntaxException e) {
                    log.error(e);
                }
            }

            return super.addBackend(backend);

        } else {
            throw new RuntimeException("ParentId not found");
        }
    }

    @Override
    public Farm delBackend(JsonObject jsonObject) {
        final Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        final String parentId = backend.getParentId();
        final String backendId = backend.getId();
        final BackendProxyClient backendPool = backendPoolsUndertow.get(parentId);
        if (backendPool!=null) {
            try {
                backendPool.removeHost(new URI(backendId));
            } catch (final URISyntaxException e) {
                log.error(e);
            }
        }
        return super.delBackend(backend);
    }

    private int maxConnPerThread() {
        final String maxConnStr = System.getProperty("io.galeb.router.maxConn");
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

    @Override
    public Farm addBackendPool(JsonObject jsonObject) {
        final BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        final Map<String, Object> properties = backendPool.getProperties();
        final String loadBalanceAlgorithm = (String) properties.get(LoadBalancePolicy.LOADBALANCE_POLICY_FIELD);
        final boolean loadBalanceDefined = loadBalanceAlgorithm!=null && LoadBalancePolicy.hasLoadBalanceAlgorithm(loadBalanceAlgorithm);

        if (!loadBalanceDefined) {
            properties.put(LoadBalancePolicy.LOADBALANCE_POLICY_FIELD, LoadBalancePolicyLocator.DEFAULT_ALGORITHM.toString());
        }

        final String backendPoolId = backendPool.getId();
        backendPoolsUndertow.put(
                                backendPoolId,
                                new BackendProxyClient().setConnectionsPerThread(maxConnPerThread())
                                                        .addSessionCookieName("JSESSIONID")
                                                        .setParams(properties));
        return super.addBackendPool(backendPool);
    }

    @Override
    public Farm delBackendPool(JsonObject jsonObject) {
        final BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        final String backendPoolId = backendPool.getId();
        backendPoolsUndertow.remove(backendPoolId);
        return super.delBackendPool(backendPool);
    }

    @Override
    public Farm addRule(JsonObject jsonObject) {
        final Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        final String virtualhostId = rule.getParentId();
        final String match = (String)rule.getProperties().get("match");
        final String targetId = (String)rule.getProperties().get("targetId");
        final int maxRequestTime = 30000;

        final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) virtualHostHandler).getHosts();
        if (!hosts.containsKey(virtualhostId)) {
            throw new RuntimeException("ParentId not found");
        }

        if (!"404".equals(targetId)) {
            final BackendProxyClient backendPool = backendPoolsUndertow.get(targetId);
            if (backendPool==null) {
                throw new RuntimeException("TargetId not found");
            }
            HttpHandler ruleHandler = hosts.get(virtualhostId);
            if (!(ruleHandler instanceof PathHandler)) {
                ruleHandler = new PathHandler(ResponseCodeHandler.HANDLE_404);
            }
            final HttpHandler targetHandler = new ProxyHandler(backendPool, maxRequestTime, ResponseCodeHandler.HANDLE_404);
            ((PathHandler) ruleHandler).addPrefixPath(match, targetHandler);
            hosts.put(virtualhostId, ruleHandler);
        }

        return super.addRule(rule);
    }

    @Override
    public Farm delRule(JsonObject jsonObject) {
        final Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        final String virtualhostId = rule.getParentId();
        final String match = (String)rule.getProperties().get("match");

        final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) virtualHostHandler).getHosts();
        final HttpHandler ruleHandler = hosts.get(virtualhostId);
        if (ruleHandler!=null && ruleHandler instanceof PathHandler) {
            ((PathHandler)ruleHandler).removePrefixPath(match);
        }
        return super.delRule(rule);
    }

    @Override
    public Farm addVirtualHost(JsonObject jsonObject) {
        final VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        final String virtualhostId = virtualhost.getId();
        ((NameVirtualHostHandler) virtualHostHandler).addHost(virtualhostId, ResponseCodeHandler.HANDLE_404);
        return super.addVirtualHost(virtualhost);
    }

    @Override
    public Farm delVirtualHost(JsonObject jsonObject) {
        final VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        final String virtualhostId = virtualhost.getId();
        for (final Rule rule: virtualhost.getRules()) {
            delRule(rule.getId());
        }
        ((NameVirtualHostHandler) virtualHostHandler).removeHost(virtualhostId);
        return super.delVirtualHost(jsonObject);
    }


}

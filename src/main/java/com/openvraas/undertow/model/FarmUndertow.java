package com.openvraas.undertow.model;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.server.handlers.proxy.ExclusivityChecker;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.Headers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

import com.openvraas.core.json.JsonObject;
import com.openvraas.core.loadbalance.LoadBalancePolicy;
import com.openvraas.core.loadbalance.LoadBalancePolicyLocator;
import com.openvraas.core.model.Backend;
import com.openvraas.core.model.Backend.Health;
import com.openvraas.core.model.BackendPool;
import com.openvraas.core.model.Farm;
import com.openvraas.core.model.Rule;
import com.openvraas.core.model.VirtualHost;
import com.openvraas.undertow.handlers.CustomLoadBalancingProxyClient;
import com.openvraas.undertow.handlers.HostMetricsHandler;

@Default
public class FarmUndertow extends Farm {

    private static final long serialVersionUID = 1L;

    private static final String LOGPATTERN = "%h %l %u %t \"%r\" %s %b (%v -> %{i,X-Proxy-Host} [%D]ms \"X-Real-IP: %{i,X-Real-IP}\" \"X-Forwarded-For: %{i,X-Forwarded-For}\")";

    private final HttpHandler virtualHostHandler = new NameVirtualHostHandler();

    private final HttpHandler hostMetricsHandler = new HostMetricsHandler(virtualHostHandler);

    private final HttpHandler rootHandler = new AccessLogHandler(hostMetricsHandler, new AccessLogReceiver() {

        public static final String DEFAULT_CATEGORY = "com.openvraas.accesslog";

        private final ExtendedLogger logger = LogManager.getContext().getLogger(DEFAULT_CATEGORY);

        @Override
        public void logMessage(String message) {
            logger.info(message);
        }

    }, LOGPATTERN, FarmUndertow.class.getClassLoader());

    private final Map<String, CustomLoadBalancingProxyClient> backendPoolsUndertow = new HashMap<>();

    public FarmUndertow() {
        super();
    }

    @Override
    public Farm setOptions(Map<String, String> options) {
        ((HostMetricsHandler) hostMetricsHandler).enabled("true".equals(options.get("EnableMetrics")));
        return super.setOptions(options);
    }

    @Override
    public HttpHandler getRootHandler() {
        return rootHandler;
    }

    @Override
    public Farm addBackend(JsonObject jsonObject) throws Exception {
        Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        String parentId = backend.getParentId();
        String backendId = backend.getId();
        Backend.Health backendHealth = backend.getHealth();

        CustomLoadBalancingProxyClient backendPool = backendPoolsUndertow.get(parentId);
        if (backendPool!=null) {
            if (backendHealth==Health.HEALTHY) {
                try {
                    backendPool.addHost(new URI(backendId));
                } catch (URISyntaxException e) {
                    throw (URISyntaxException)e;
                }
            } else {
                try {
                    backendPool.removeHost(new URI(backendId));
                } catch (URISyntaxException e) {
                    throw (URISyntaxException)e;
                }
            }

            return super.addBackend(backend);

        } else {
            throw new RuntimeException("ParentId not found");
        }
    }

    @Override
    public Farm delBackend(JsonObject jsonObject) throws Exception {
        Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        String parentId = backend.getParentId();
        String backendId = backend.getId();
        CustomLoadBalancingProxyClient backendPool = backendPoolsUndertow.get(parentId);
        if (backendPool!=null) {
            try {
                backendPool.removeHost(new URI(backendId));
            } catch (URISyntaxException e) {
                throw (URISyntaxException)e;
            }
        }
        return super.delBackend(backend);
    }

    @Override
    public Farm addBackendPool(JsonObject jsonObject) {
        BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        Map<String, Object> properties = backendPool.getProperties();
        String loadBalanceAlgorithm = (String) properties.get(LoadBalancePolicy.LOADBALANCE_POLICY_FIELD);
        boolean loadBalanceDefined = loadBalanceAlgorithm!=null && LoadBalancePolicy.hasLoadBalanceAlgorithm(loadBalanceAlgorithm);

        if (!loadBalanceDefined) {
            properties.put(LoadBalancePolicy.LOADBALANCE_POLICY_FIELD, LoadBalancePolicyLocator.DEFAULT_ALGORITHM.toString());
        }

        String backendPoolId = backendPool.getId();
        backendPoolsUndertow.put(backendPoolId, new CustomLoadBalancingProxyClient(new ExclusivityChecker() {
            @Override
            public boolean isExclusivityRequired(HttpServerExchange exchange) {
                //we always create a new connection for upgrade requests
                return exchange.getRequestHeaders().contains(Headers.UPGRADE);
            }
        }).setConnectionsPerThread(20)
          .addSessionCookieName("JSESSIONID")
          .setParams(properties));
        return super.addBackendPool(backendPool);
    }

    @Override
    public Farm delBackendPool(JsonObject jsonObject) {
        BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        String backendPoolId = backendPool.getId();
        backendPoolsUndertow.remove(backendPoolId);
        return super.delBackendPool(backendPool);
    }

    @Override
    public Farm addRule(JsonObject jsonObject) {
        Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        String virtualhostId = rule.getParentId();
        String match = ((String)rule.getProperties().get("match"));
        String targetId = ((String)rule.getProperties().get("targetId"));
        int maxRequestTime = 30000;

        final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) virtualHostHandler).getHosts();
        if (!hosts.containsKey(virtualhostId)) {
            throw new RuntimeException("ParentId not found");
        }

        if (!"404".equals(targetId)) {
            CustomLoadBalancingProxyClient backendPool = backendPoolsUndertow.get(targetId);
            if (backendPool==null) {
                throw new RuntimeException("TargetId not found");
            }
            HttpHandler ruleHandler = hosts.get(virtualhostId);
            if (!(ruleHandler instanceof PathHandler)) {
                ruleHandler = new PathHandler(ResponseCodeHandler.HANDLE_404);
            }
            ruleHandler = new PathHandler(ResponseCodeHandler.HANDLE_404);
            HttpHandler targetHandler = new ProxyHandler(backendPool, maxRequestTime, ResponseCodeHandler.HANDLE_404);
            ((PathHandler) ruleHandler).addPrefixPath(match, targetHandler);
            hosts.put(virtualhostId, ruleHandler);
        }

        return super.addRule(rule);
    }

    @Override
    public Farm delRule(JsonObject jsonObject) {
        Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        String virtualhostId = rule.getParentId();
        String match = ((String)rule.getProperties().get("match"));

        final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) virtualHostHandler).getHosts();
        HttpHandler ruleHandler = hosts.get(virtualhostId);
        if (ruleHandler!=null) {
            ((PathHandler)ruleHandler).removePrefixPath(match);
        }
        return super.delRule(rule);
    }

    @Override
    public Farm addVirtualHost(JsonObject jsonObject) {
        VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        String virtualhostId = virtualhost.getId();
        ((NameVirtualHostHandler) virtualHostHandler).addHost(virtualhostId, ResponseCodeHandler.HANDLE_404);
        return super.addVirtualHost(virtualhost);
    }

    @Override
    public Farm delVirtualHost(JsonObject jsonObject) {
        VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        String virtualhostId = virtualhost.getId();
        for (Rule rule: virtualhost.getRules()) {
            delRule(rule.getId());
        }
        ((NameVirtualHostHandler) virtualHostHandler).removeHost(virtualhostId);
        return super.delVirtualHost(jsonObject);
    }


}

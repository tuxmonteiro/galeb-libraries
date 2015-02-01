package com.openvraas.undertow.model;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;

import com.openvraas.core.json.JsonObject;
import com.openvraas.core.model.Backend;
import com.openvraas.core.model.BackendPool;
import com.openvraas.core.model.Farm;
import com.openvraas.core.model.Rule;
import com.openvraas.core.model.VirtualHost;

@Default
public class FarmUndertow extends Farm {

    private static final long serialVersionUID = 1L;

    private final HttpHandler rootHandler = new NameVirtualHostHandler();

    private final Map<String, LoadBalancingProxyClient> backendPools = new HashMap<>();

    public FarmUndertow() {
        super();
    }

    @Override
    public HttpHandler getRootHandler() {
        return rootHandler;
    }

    @Override
    public Farm addBackend(JsonObject jsonObject) {
        Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        String parentId = backend.getParentId();
        String backendId = backend.getId();

        LoadBalancingProxyClient backendPool = backendPools.get(parentId);
        if (backendPool!=null) {
            try {
                backendPool.addHost(new URI(backendId));
            } catch (URISyntaxException e) {
                //log error
            }
        }
        return super.addBackend(backend);
    }

    @Override
    public Farm delBackend(JsonObject jsonObject) {
        Backend backend = (Backend) JsonObject.fromJson(jsonObject.toString(), Backend.class);
        String parentId = backend.getParentId();
        String backendId = backend.getId();
        LoadBalancingProxyClient backendPool = backendPools.get(parentId);
        if (backendPool!=null) {
            try {
                backendPool.removeHost(new URI(backendId));
            } catch (URISyntaxException e) {
                //log error
            }
        }
        return super.delBackend(backend);
    }

    @Override
    public Farm addBackendPool(JsonObject jsonObject) {
        BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        String backendPoolId = backendPool.getId();
        backendPools.put(backendPoolId, new LoadBalancingProxyClient().setConnectionsPerThread(20));
        return super.addBackendPool(backendPool);
    }

    @Override
    public Farm delBackendPool(JsonObject jsonObject) {
        BackendPool backendPool = (BackendPool) JsonObject.fromJson(jsonObject.toString(), BackendPool.class);
        String backendPoolId = backendPool.getId();
        for (VirtualHost virtualhost: super.getVirtualHosts()) {
            for (Rule rule: virtualhost.getRules()) {
                String targetId = rule.getProperties().get("targetId").toString();
                if (targetId!=null && targetId.equals(backendPoolId)) {
                    delRule(rule);
                }
            }
        }
        backendPools.remove(backendPool);
        return super.delBackendPool(backendPool);
    }

    @Override
    public Farm addRule(JsonObject jsonObject) {
        Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        String virtualhostId = rule.getParentId();
        String match = ((String)rule.getProperties().get("match"));
        String targetId = ((String)rule.getProperties().get("targetId"));

        LoadBalancingProxyClient backendPool = backendPools.get(targetId);
        if (backendPool!=null) {
            HttpHandler backendPoolHandler = new ProxyHandler(backendPool, 30000, ResponseCodeHandler.HANDLE_404);
            final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) rootHandler).getHosts();
            HttpHandler ruleHandler = hosts.get(virtualhostId);
            if (ruleHandler==null) {
                ruleHandler = new PathHandler(ResponseCodeHandler.HANDLE_404);
            } else {
                if (ResponseCodeHandler.class.getSimpleName().equals(ruleHandler.getClass().getSimpleName())) {
                    ruleHandler = new PathHandler(ResponseCodeHandler.HANDLE_404);
                }
            }
            ((PathHandler)ruleHandler).addPrefixPath(match, backendPoolHandler);
            hosts.put(virtualhostId, ruleHandler);
        }
        return super.addRule(rule);
    }

    @Override
    public Farm delRule(JsonObject jsonObject) {
        Rule rule = (Rule) JsonObject.fromJson(jsonObject.toString(), Rule.class);
        String virtualhostId = rule.getParentId();
        String match = ((String)rule.getProperties().get("match"));

        final Map<String, HttpHandler> hosts = ((NameVirtualHostHandler) rootHandler).getHosts();
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
        ((NameVirtualHostHandler) rootHandler).addHost(virtualhostId, ResponseCodeHandler.HANDLE_404);
        return super.addVirtualHost(virtualhost);
    }

    @Override
    public Farm delVirtualHost(JsonObject jsonObject) {
        VirtualHost virtualhost = (VirtualHost) JsonObject.fromJson(jsonObject.toString(), VirtualHost.class);
        String virtualhostId = virtualhost.getId();
        for (Rule rule: virtualhost.getRules()) {
            delRule(rule.getId());
        }
        ((NameVirtualHostHandler) rootHandler).removeHost(virtualhostId);
        return super.delVirtualHost(jsonObject);
    }


}

package io.galeb.undertow.handlers;

import io.undertow.client.UndertowClient;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.ExclusivityChecker;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyCallback;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyConnection;
import io.undertow.util.Headers;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.xnio.OptionMap;
import org.xnio.ssl.XnioSsl;

public class BackendProxyClient implements ProxyClient {

    private final LoadBalancingProxyClient loadBalanceProxyClient;
    private final BackendSelector hostSelectorHandler;
    private final ExclusivityChecker exclusivityChecker;

    public BackendProxyClient() {
        hostSelectorHandler = new BackendSelector();
        exclusivityChecker = new ExclusivityChecker() {
            @Override
            public boolean isExclusivityRequired(HttpServerExchange exchange) {
                // we always create a new connection for upgrade requests
                return exchange.getRequestHeaders().contains(Headers.UPGRADE);
            }
        };
        loadBalanceProxyClient = new LoadBalancingProxyClient(
                UndertowClient.getInstance(), exclusivityChecker,
                hostSelectorHandler);
    }

    @Override
    public ProxyTarget findTarget(final HttpServerExchange exchange) {
        hostSelectorHandler.setExchange(exchange);
        return loadBalanceProxyClient.findTarget(exchange);
    }

    @Override
    public void getConnection(final ProxyTarget target,
            final HttpServerExchange exchange,
            final ProxyCallback<ProxyConnection> callback, long timeout, TimeUnit timeUnit) {

        hostSelectorHandler.setExchange(exchange);
        loadBalanceProxyClient.getConnection(target, exchange, callback, timeout, timeUnit);
    }

    public BackendProxyClient addSessionCookieName(
            final String sessionCookieName) {
        loadBalanceProxyClient.addSessionCookieName(sessionCookieName);
        return this;
    }

    public BackendProxyClient removeSessionCookieName(
            final String sessionCookieName) {
        loadBalanceProxyClient.removeSessionCookieName(sessionCookieName);
        return this;
    }

    public BackendProxyClient setProblemServerRetry(int problemServerRetry) {
        loadBalanceProxyClient.setProblemServerRetry(problemServerRetry);
        return this;
    }

    public int getProblemServerRetry() {
        return loadBalanceProxyClient.getProblemServerRetry();
    }

    public int getConnectionsPerThread() {
        return loadBalanceProxyClient.getConnectionsPerThread();
    }

    public BackendProxyClient setConnectionsPerThread(int connectionsPerThread) {
        loadBalanceProxyClient.setConnectionsPerThread(connectionsPerThread);
        return this;
    }

    public int getMaxQueueSize() {
        return loadBalanceProxyClient.getMaxQueueSize();
    }

    public BackendProxyClient setMaxQueueSize(int maxQueueSize) {
        loadBalanceProxyClient.setMaxQueueSize(maxQueueSize);
        return this;
    }

    public BackendProxyClient setTtl(int ttl) {
        loadBalanceProxyClient.setTtl(ttl);
        return this;
    }

    public BackendProxyClient setSoftMaxConnectionsPerThread(
            int softMaxConnectionsPerThread) {
        loadBalanceProxyClient
                .setSoftMaxConnectionsPerThread(softMaxConnectionsPerThread);
        return this;
    }

    public synchronized BackendProxyClient setParams(
            final Map<String, Object> myParams) {
        hostSelectorHandler.setParams(myParams);
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host) {
        loadBalanceProxyClient.addHost(host);
        hostSelectorHandler.reset();
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host, XnioSsl ssl) {

        loadBalanceProxyClient.addHost(host, ssl);
        hostSelectorHandler.reset();
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute) {

        loadBalanceProxyClient.addHost(host, jvmRoute);
        hostSelectorHandler.reset();
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute, XnioSsl ssl) {

        loadBalanceProxyClient.addHost(host, jvmRoute, ssl);
        hostSelectorHandler.reset();
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute, XnioSsl ssl, OptionMap options) {

        loadBalanceProxyClient.addHost(host, jvmRoute, ssl, options);
        hostSelectorHandler.reset();
        return this;
    }

    public synchronized BackendProxyClient addHost(
            final InetSocketAddress bindAddress, final URI host,
            String jvmRoute, XnioSsl ssl, OptionMap options) {

        loadBalanceProxyClient.addHost(bindAddress, host, jvmRoute, ssl,
                options);
        hostSelectorHandler.reset();
        return this;
    }

    public synchronized BackendProxyClient removeHost(final URI host) {
        loadBalanceProxyClient.removeHost(host);
        hostSelectorHandler.reset();
        return this;
    }
}

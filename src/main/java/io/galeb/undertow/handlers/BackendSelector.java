package io.galeb.undertow.handlers;

import io.galeb.core.loadbalance.LoadBalancePolicy;
import io.galeb.core.loadbalance.LoadBalancePolicyLocator;
import io.galeb.undertow.nullable.FakeHttpServerExchange;
import io.galeb.undertow.util.UndertowSourceIP;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.Host;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.HostSelector;
import io.undertow.util.CopyOnWriteMap;
import io.undertow.util.HttpString;

import java.util.Map;

public class BackendSelector implements HostSelector {

    private HttpServerExchange exchange = FakeHttpServerExchange.NULL;

    private final Map<String, Object> params = new CopyOnWriteMap<>();
    private volatile LoadBalancePolicy loadBalancePolicy = LoadBalancePolicy.NULL;
    private final LoadBalancePolicyLocator loadBalancePolicyLocator = new LoadBalancePolicyLocator();

    @Override
    public int selectHost(final Host[] availableHosts) {
        if (loadBalancePolicy == LoadBalancePolicy.NULL) {
            loadBalancePolicy = loadBalancePolicyLocator.setParams(params).get();
            loadBalancePolicy.reset();
        }

        final int hostID = loadBalancePolicy.setCriteria(params)
                                            .setCriteria(new UndertowSourceIP(), exchange)
                                            .mapOfHosts(availableHosts).getChoice();

        final Host host = availableHosts[hostID];
        if (host != null) {
            makeTraceable(host);
        }

        return hostID;
    }

    private void makeTraceable(final Host host) {
        final HttpString xproxyhost = new HttpString("X-Proxy-Host");
        final HttpString xstarttime = new HttpString("X-Start-Time");

        exchange.getRequestHeaders().put(
                xproxyhost,
                host != null ? host.getUri().toString() : "UNDEF");
        exchange.getRequestHeaders().put(
                xstarttime,
                System.nanoTime());
    }

    public HostSelector setParams(final Map<String, Object> myParams) {
        if (myParams != null) {
            params.putAll(myParams);
        }
        return this;
    }

    public void reset() {
        loadBalancePolicy = LoadBalancePolicy.NULL;
    }

    public HostSelector setExchange(final HttpServerExchange exchange) {
        this.exchange = exchange;
        return this;
    }

}

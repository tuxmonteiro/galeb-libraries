package io.galeb.undertow.handlers;

import io.galeb.core.loadbalance.LoadBalancePolicy;
import io.galeb.core.loadbalance.LoadBalancePolicyLocator;
import io.galeb.core.logging.Logger;
import io.galeb.undertow.nullable.FakeHttpServerExchange;
import io.galeb.undertow.util.UndertowSourceIP;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.Host;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.HostSelector;
import io.undertow.util.CopyOnWriteMap;
import io.undertow.util.HttpString;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class BackendSelector implements HostSelector {

    // Usefull Custom HTTP Headers
    public static final String X_START_TIME = "X-Start-Time";
    public static final String X_PROXY_HOST = "X-Proxy-Host";

    private HttpServerExchange exchange = FakeHttpServerExchange.NULL;

    private final Map<String, Object> params = new CopyOnWriteMap<>();
    private volatile LoadBalancePolicy loadBalancePolicy = LoadBalancePolicy.NULL;
    private final LoadBalancePolicyLocator loadBalancePolicyLocator = new LoadBalancePolicyLocator();
    private final List<URI> uris = new LinkedList<>();

    @Inject
    private Logger logger;

    @Override
    public int selectHost(final Host[] availableHosts) {
        if (loadBalancePolicy == LoadBalancePolicy.NULL) {
            loadBalancePolicy = loadBalancePolicyLocator.setParams(params).get();
            loadBalancePolicy.reset();
            uris.clear();
            for (int x=0;x<availableHosts.length;x++) {
                final Host host = availableHosts[x];
                if (host!=null) {
                    uris.add(host.getUri());
                }
            }
        }

        final int hostID = loadBalancePolicy.setCriteria(params)
                                            .setCriteria(new UndertowSourceIP(), exchange)
                                            .mapOfHosts(uris).getChoice();

        try {
            trace(availableHosts[hostID]);
        } catch (final IndexOutOfBoundsException e) {
            logger.error(e);
            return 0;
        }

        return hostID;
    }

    private void trace(final Host host) {
        final HttpString xproxyhost = new HttpString(X_PROXY_HOST);
        final HttpString xstarttime = new HttpString(X_START_TIME);

        exchange.getRequestHeaders().put(
                xproxyhost,
                host != null ? host.getUri().toString() : "UNDEF");
        exchange.getRequestHeaders().put(
                xstarttime,
                System.nanoTime());
    }

    public HostSelector setParams(final Map<String, Object> myParams) {
        if (myParams != null) {
            params.clear();
            params.putAll(myParams);
        }
        return this;
    }

    public HostSelector addParam(String paramId, Object param) {
        params.put(paramId, param);
        return this;
    }

    public HostSelector removeParam(String paramId, Object param) {
        params.remove(paramId);
        return this;
    }

    public void reset() {
        loadBalancePolicy = LoadBalancePolicy.NULL;
    }

    public synchronized HostSelector setExchange(final HttpServerExchange exchange) {
        this.exchange = exchange;
        return this;
    }

}

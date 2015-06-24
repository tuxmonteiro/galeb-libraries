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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.xnio.OptionMap;
import org.xnio.ssl.XnioSsl;

public class BackendProxyClient implements ProxyClient {

    private final LoadBalancingProxyClient loadBalanceProxyClient;
    private final BackendSelector hostSelectorHandler = new BackendSelector();
    private final Set<URI> hosts = new CopyOnWriteArraySet<>();

    public BackendProxyClient() {
        final ExclusivityChecker exclusivityChecker = new ExclusivityChecker() {
            @Override
            public boolean isExclusivityRequired(HttpServerExchange exchange) {
                // we always create a new connection for upgrade requests
                return exchange.getRequestHeaders().contains(Headers.UPGRADE);
            }
        };
        loadBalanceProxyClient = new LoadBalancingProxyClient(
                                        UndertowClient.getInstance(),
                                        exclusivityChecker,
                                        hostSelectorHandler);
    }

    @Override
    public ProxyTarget findTarget(final HttpServerExchange exchange) {
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
        if (!hosts.contains(host)) {
            loadBalanceProxyClient.addHost(host);
            hosts.add(host);
            hostSelectorHandler.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host, XnioSsl ssl) {
        if (!hosts.contains(host)) {
            loadBalanceProxyClient.addHost(host, ssl);
            hosts.add(host);
            hostSelectorHandler.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute) {
        if (!hosts.contains(host)) {
            loadBalanceProxyClient.addHost(host, jvmRoute);
            hosts.add(host);
            hostSelectorHandler.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute, XnioSsl ssl) {
        if (!hosts.contains(host)) {
            loadBalanceProxyClient.addHost(host, jvmRoute, ssl);
            hosts.add(host);
            hostSelectorHandler.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute, XnioSsl ssl, OptionMap options) {
        if (!hosts.contains(host)) {
            loadBalanceProxyClient.addHost(host, jvmRoute, ssl, options);
            hosts.add(host);
            hostSelectorHandler.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(
            final InetSocketAddress bindAddress, final URI host,
            String jvmRoute, XnioSsl ssl, OptionMap options) {
        if (!hosts.contains(host)) {
            loadBalanceProxyClient.addHost(bindAddress, host, jvmRoute, ssl,
                    options);
            hosts.add(host);
            hostSelectorHandler.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient removeHost(final URI host) {
        if (hosts.contains(host)) {
            loadBalanceProxyClient.removeHost(host);
            hosts.remove(host);
            hostSelectorHandler.reset();
        }
        return this;
    }

    public void reset() {
        hostSelectorHandler.reset();
    }
}

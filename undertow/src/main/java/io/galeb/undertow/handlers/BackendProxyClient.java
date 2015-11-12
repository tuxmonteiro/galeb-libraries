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

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.undertow.UndertowLogger;
import io.undertow.client.ClientConnection;
import io.undertow.client.UndertowClient;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.ssl.XnioSsl;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.ExclusivityChecker;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyCallback;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyConnection;

public class BackendProxyClient implements ProxyClient {

    private final LoadBalancingProxyClient loadBalanceProxyClient;
    private final BackendSelector backendSelector = new BackendSelector();

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
                                        backendSelector);
    }

    @Override
    public ProxyTarget findTarget(final HttpServerExchange exchange) {
        return loadBalanceProxyClient.findTarget(exchange);
    }

    @Override
    public void getConnection(final ProxyTarget target,
            final HttpServerExchange exchange,
            final ProxyCallback<ProxyConnection> callback, long timeout, TimeUnit timeUnit) {

        backendSelector.setExchange(exchange);
        final ProxyCallback<ProxyConnection> newCallback = new LocalProxyCallback(callback);
        loadBalanceProxyClient.getConnection(target, exchange, newCallback, timeout, timeUnit);
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
        backendSelector.setParams(myParams);
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host) {
        if (!backendSelector.contains(host)) {
            loadBalanceProxyClient.addHost(host);
            backendSelector.addHost(host);
            backendSelector.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host, XnioSsl ssl) {
        if (!backendSelector.contains(host)) {
            loadBalanceProxyClient.addHost(host, ssl);
            backendSelector.addHost(host);
            backendSelector.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute) {
        if (!backendSelector.contains(host)) {
            loadBalanceProxyClient.addHost(host, jvmRoute);
            backendSelector.addHost(host);
            backendSelector.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute, XnioSsl ssl) {
        if (!backendSelector.contains(host)) {
            loadBalanceProxyClient.addHost(host, jvmRoute, ssl);
            backendSelector.addHost(host);
            backendSelector.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(final URI host,
            String jvmRoute, XnioSsl ssl, OptionMap options) {
        if (!backendSelector.contains(host)) {
            loadBalanceProxyClient.addHost(host, jvmRoute, ssl, options);
            backendSelector.addHost(host);
            backendSelector.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient addHost(
            final InetSocketAddress bindAddress, final URI host,
            String jvmRoute, XnioSsl ssl, OptionMap options) {
        if (!backendSelector.contains(host)) {
            loadBalanceProxyClient.addHost(bindAddress, host, jvmRoute, ssl,
                    options);
            backendSelector.addHost(host);
            backendSelector.reset();
        }
        return this;
    }

    public synchronized BackendProxyClient removeHost(final URI host) {
        if (backendSelector.contains(host)) {
            loadBalanceProxyClient.removeHost(host);
            backendSelector.removeHost(host);
            backendSelector.reset();
        }
        return this;
    }

    public void reset() {
        backendSelector.reset();
    }

    private final static class LocalProxyCallback implements ProxyCallback<ProxyConnection> {

        private static final AttachmentKey<ProxyConnection> CONNECTION = AttachmentKey.create(ProxyConnection.class);

        private final ProxyCallback<ProxyConnection> callback;

        public LocalProxyCallback(ProxyCallback<ProxyConnection> callback) {
            this.callback = callback;
        }

        @Override
        public void completed(HttpServerExchange exchange, ProxyConnection result) {
            callback.completed(exchange, result);
        }

        @Override
        public void failed(HttpServerExchange exchange) {
            exchange.removeAttachment(BackendSelector.REAL_DEST);
            callback.failed(exchange);
        }

        @Override
        public void couldNotResolveBackend(HttpServerExchange exchange) {
            exchange.removeAttachment(BackendSelector.REAL_DEST);
            callback.couldNotResolveBackend(exchange);
        }

        @Override
        public void queuedRequestFailed(HttpServerExchange exchange) {
            exchange.removeAttachment(BackendSelector.REAL_DEST);
            callback.queuedRequestFailed(exchange);
        }

        void cancel(final HttpServerExchange exchange) {
            final ProxyConnection connectionAttachment = exchange.getAttachment(CONNECTION);
            if (connectionAttachment != null) {
                ClientConnection clientConnection = connectionAttachment.getConnection();
                UndertowLogger.REQUEST_LOGGER.timingOutRequest(clientConnection.getPeerAddress() + "" + exchange.getRequestURI());
                IoUtils.safeClose(clientConnection);
            } else {
                UndertowLogger.REQUEST_LOGGER.timingOutRequest(exchange.getRequestURI());
            }
            if (exchange.isResponseStarted()) {
                IoUtils.safeClose(exchange.getConnection());
            } else {
                exchange.setResponseCode(StatusCodes.SERVICE_UNAVAILABLE);
                exchange.endExchange();
            }
        }
    }
}

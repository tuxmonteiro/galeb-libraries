/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

import static io.galeb.core.extractable.RequestCookie.DEFAULT_COOKIE;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import io.galeb.core.loadbalance.LoadBalancePolicy;
import io.galeb.core.loadbalance.LoadBalancePolicyLocator;
import io.galeb.core.loadbalance.impl.HashPolicy;
import io.galeb.core.util.consistenthash.HashAlgorithm;
import io.galeb.core.util.consistenthash.HashAlgorithm.HashType;
import io.galeb.undertow.extractable.UndertowCookie;
import io.galeb.undertow.loadbalance.hash.UndertowKeyTypeLocator;
import io.galeb.undertow.nullable.FakeHttpServerExchange;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.Host;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.HostSelector;
import io.undertow.util.AttachmentKey;
import io.undertow.util.CopyOnWriteMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackendSelector implements HostSelector {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final AttachmentKey<String> REAL_DEST    = AttachmentKey.create(String.class);
    public static final String                HOST_UNDEF   = "http://UNDEF:0";
    public static final String                STICK_COOKIE = "GALEB-GRAVITON";

    private HttpServerExchange exchange = FakeHttpServerExchange.NULL;

    private final Map<String, Object> params = new CopyOnWriteMap<>();
    private volatile LoadBalancePolicy loadBalancePolicy = LoadBalancePolicy.NULL;
    private final LoadBalancePolicyLocator loadBalancePolicyLocator = new LoadBalancePolicyLocator();
    private final Map<String, URI> hosts = Collections.synchronizedMap(new LinkedHashMap<>());
    private final HashAlgorithm hashAlgorithm = new HashAlgorithm(HashType.MD5);
    private Boolean enabledStickCookie = null;

    @Override
    public int selectHost(Host[] availableHosts) {
        if (loadBalancePolicy == LoadBalancePolicy.NULL) {
            loadBalancePolicy = loadBalancePolicyLocator.setParams(params).get();
            loadBalancePolicy.setKeyTypeLocator(UndertowKeyTypeLocator.INSTANCE).reset();
            enabledStickCookie = null;
            if (loadBalancePolicy instanceof HashPolicy) {
                final Host[] hostOrdered = Arrays.stream(availableHosts)
                                    .sorted(Comparator.comparing(Host::getUri))
                                    .collect(Collectors.toCollection(LinkedList::new)).toArray(new Host[availableHosts.length - 1]);
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (availableHosts) {
                    availableHosts = hostOrdered;
                }
            }
        }
        if (enabledStickCookie==null) {
            enabledStickCookie = params.get(LoadBalancePolicy.PROP_STICK) != null;
        }
        int hostID = -1;
        if (enabledStickCookie) {
            hostID = findStickHostID(availableHosts);
        }
        hostID = hostID > -1 ? hostID : getChoice(availableHosts);
        try {
            final Host host = availableHosts[hostID > -1 ? hostID : 0];
            if (host!=null) {
                if (enabledStickCookie) {
                    setStickCookie(host.getUri().toString());
                }
                final URI uri = host.getUri();
                trace(uri != null ? uri.toString() : "UNDEF");
            }
        } catch (final IndexOutOfBoundsException e) {
            LOGGER.error(e);
            return 0;
        }

        return hostID;
    }

    private int getChoice(final Host[] availableHosts) {
        return loadBalancePolicy.setCriteria(params)
                                      .extractKeyFrom(exchange)
                                      .mapOfHosts(Arrays.stream(availableHosts)
                                              .map(host -> host.getUri().toString())
                                              .sorted()
                                              .collect(Collectors.toCollection(LinkedList::new)))
                                      .getChoice();
    }

    private void setStickCookie(final String host) {
        final String hash = hashAlgorithm.hash(host).asString();
        Cookie stickCookie = new CookieImpl(STICK_COOKIE, hash);
        stickCookie.setPath("/");
        exchange.getResponseCookies().put(STICK_COOKIE, stickCookie);
    }

    private int findStickHostID(final Host[] availableHosts) {
        int hostID = -1;
        String stickCookie = new UndertowCookie().from(STICK_COOKIE).get(exchange);
        if (!stickCookie.equals(DEFAULT_COOKIE)) {
            final URI uri = hosts.get(stickCookie);
            for (int pos=0; uri != null && pos<availableHosts.length; pos++) {
                final Host host = availableHosts[pos];
                if (host.getUri().equals(uri)) {
                    hostID = pos;
                    break;
                }
            }
        }
        return hostID;
    }

    private void trace(final String uri) {
        if (uri!=null) {
            exchange.putAttachment(REAL_DEST, uri);
        }
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

    public Map<String, URI> getHosts() {
        return hosts;
    }

    public boolean addHost(URI host) {
        final String hash = hashAlgorithm.hash(host.toString()).asString();
        return hosts.putIfAbsent(hash, host) == null;
    }

    public boolean removeHost(URI host) {
        final String hash = hashAlgorithm.hash(host.toString()).asString();
        return hosts.remove(hash, host);
    }

    public boolean contains(URI host) {
        final String hash = hashAlgorithm.hash(host.toString()).asString();
        return hosts.containsKey(hash);
    }

    public boolean contains(String hash) {
        return hosts.containsKey(hash);
    }

}

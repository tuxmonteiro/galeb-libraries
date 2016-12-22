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

import static io.galeb.core.extractable.RequestCookie.DEFAULT_COOKIE;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.galeb.core.loadbalance.LoadBalancePolicy;
import io.galeb.core.loadbalance.LoadBalancePolicyLocator;
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
    private final Map<String, ExpirableURI> hosts = Collections.synchronizedMap(new LinkedHashMap<>());
    private final HashAlgorithm hashAlgorithm = new HashAlgorithm(HashType.MD5);
    private Boolean enabledStickCookie = null;
    private long timeOutExpirableURI = 60000; // 60 sec

    public static class ExpirableURI {
        private final URI uri;
        private long statusTime;
        private boolean quarantine;

        ExpirableURI(final URI uri) {
            this.uri = uri;
        }

        public URI getUri() {
            return uri;
        }

        public long getStatusTime() {
            return statusTime;
        }

        private void updateStatusTime() {
            this.statusTime = System.currentTimeMillis();
        }

        public boolean isQuarantine() {
            return quarantine;
        }

        public void setQuarantine(boolean quarantine) {
            this.quarantine = quarantine;
            updateStatusTime();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExpirableURI that = (ExpirableURI) o;
            return Objects.equals(uri, that.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri);
        }
    }

    public void cleanUpMapExpirableURI() {
        final Stream<Map.Entry<String, ExpirableURI>> expirablesURIStream = hosts.entrySet().stream();
        expirablesURIStream.filter(e -> e.getValue().getStatusTime() > System.currentTimeMillis() - timeOutExpirableURI)
                           .forEach(e -> hosts.remove(e.getKey()));
    }

    public BackendSelector setTimeOutExpirableURI(long timeOutExpirableURI) {
        this.timeOutExpirableURI = timeOutExpirableURI;
        return this;
    }

    @Override
    public int selectHost(final Host[] availableHosts) {
        if (loadBalancePolicy == LoadBalancePolicy.NULL) {
            loadBalancePolicy = loadBalancePolicyLocator.setParams(params).get();
            loadBalancePolicy.setKeyTypeLocator(UndertowKeyTypeLocator.INSTANCE).reset();
            enabledStickCookie = null;
        }
        if (enabledStickCookie==null) {
            enabledStickCookie = params.get(LoadBalancePolicy.PROP_STICK) != null;
        }
        int hostID = -1;
        final Host[] availableHostsCopy = filterAvaliableHosts(availableHosts);
        if (enabledStickCookie) {
            hostID = findStickHostID(availableHostsCopy);
        }
        hostID = hostID > -1 ? hostID : getChoice(availableHostsCopy);
        try {
            final Host host = availableHostsCopy[hostID > -1 ? hostID : 0];
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

    private Host[] filterAvaliableHosts(final Host[] availableHosts) {
        return Arrays.stream(availableHosts)
                .filter(h -> hosts.entrySet().stream().map(Map.Entry::getValue).anyMatch(v -> v.getUri().equals(h.getUri()) && !v.isQuarantine()))
                .collect(Collectors.toList()).toArray(new Host[0]);
    }

    private int getChoice(final Host[] availableHosts) {
        return loadBalancePolicy.setCriteria(params)
                                .extractKeyFrom(exchange)
                                .mapOfHosts(Arrays.stream(availableHosts).map(host -> host.getUri().toString())
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
            final URI uri = hosts.get(stickCookie).getUri();
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
        cleanUpMapExpirableURI();
        loadBalancePolicy = LoadBalancePolicy.NULL;
    }

    public synchronized HostSelector setExchange(final HttpServerExchange exchange) {
        this.exchange = exchange;
        return this;
    }

    public boolean addHost(URI host) {
        final String hash = hashAlgorithm.hash(host.toString()).asString();
        if (contains(host)) {
            final ExpirableURI expirableURI = getExpirableURI(host);
            if (expirableURI.isQuarantine()) {
                expirableURI.setQuarantine(false);
                return true;
            }
            return false;
        } else {
            return hosts.put(hash, new ExpirableURI(host)) == null;
        }
    }

    public boolean removeHost(URI host) {
        if (contains(host)) {
            getExpirableURI(host).setQuarantine(true);
            return true;
        }
        return false;
    }

    public boolean contains(URI host) {
        final String hash = hashAlgorithm.hash(host.toString()).asString();
        return hosts.containsKey(hash);
    }

    public ExpirableURI getExpirableURI(URI host) {
        final String hash = hashAlgorithm.hash(host.toString()).asString();
        return hosts.get(hash);
    }

    public boolean contains(String hash) {
        return hosts.containsKey(hash);
    }

}

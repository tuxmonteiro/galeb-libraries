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

import io.galeb.core.loadbalance.LoadBalancePolicy;
import io.galeb.core.loadbalance.LoadBalancePolicyLocator;
import io.galeb.core.logging.Logger;
import io.galeb.undertow.nullable.FakeHttpServerExchange;
import io.galeb.undertow.util.UndertowSourceIP;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.Host;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.HostSelector;
import io.undertow.util.AttachmentKey;
import io.undertow.util.CopyOnWriteMap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class BackendSelector implements HostSelector {

    public static final AttachmentKey<String> REAL_DEST  = AttachmentKey.create(String.class);

    public static final String                HOST_UNDEF = "http://UNDEF:0";

    private HttpServerExchange exchange = FakeHttpServerExchange.NULL;

    private final Map<String, Object> params = new CopyOnWriteMap<>();
    private volatile LoadBalancePolicy loadBalancePolicy = LoadBalancePolicy.NULL;
    private final LoadBalancePolicyLocator loadBalancePolicyLocator = new LoadBalancePolicyLocator();

    @Inject
    private Logger logger;

    @Override
    public int selectHost(final Host[] availableHosts) {
        if (loadBalancePolicy == LoadBalancePolicy.NULL) {
            loadBalancePolicy = loadBalancePolicyLocator.setParams(params).get();
            loadBalancePolicy.reset();
        }
        int hostID = loadBalancePolicy.setCriteria(params)
                                      .setCriteria(new UndertowSourceIP(), exchange)
                                      .mapOfHosts(Arrays.stream(availableHosts)
                                              .map(host -> host.getUri().toString())
                                              .collect(Collectors.toCollection(LinkedList::new)))
                                      .getChoice();
        if (hostID < 0) {
            hostID = 0;
        }

        try {
            final Host host = availableHosts[hostID];
            if (host!=null) {
                trace(host.getUri().toString());
            }
        } catch (final IndexOutOfBoundsException e) {
            logger.error(e);
            return 0;
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

}

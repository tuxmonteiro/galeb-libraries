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

import io.galeb.core.statsd.StatsdClient;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class MonitorHeadersHandler implements HttpHandler {

    private final HttpHandler next;
    private int maxRequestTime = 0;
    private StatsdClient statsdClient;
    private boolean forceChangeStatus = false;

    public MonitorHeadersHandler(final HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.addExchangeCompleteListener(new HeaderMetricsListener()
                .setStatsd(statsdClient)
                .setMaxRequestTime(maxRequestTime)
                .forceChangeStatus(forceChangeStatus));
        next.handleRequest(exchange);
    }

    public MonitorHeadersHandler setStatsd(StatsdClient statsdClient) {
        this.statsdClient = statsdClient;
        return this;
    }

    public MonitorHeadersHandler setMaxRequestTime(int maxRequestTime) {
        this.maxRequestTime = maxRequestTime;
        return this;
    }

    public MonitorHeadersHandler forceChangeStatus(boolean forceChangeStatus) {
        this.forceChangeStatus = forceChangeStatus;
        return this;
    }

}

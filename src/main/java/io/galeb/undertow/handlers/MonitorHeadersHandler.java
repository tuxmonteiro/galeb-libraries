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


import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.logging.Logger;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class MonitorHeadersHandler implements HttpHandler {

    private final HttpHandler next;
    private IEventBus eventBus = IEventBus.NULL;
    private Logger logger;

    public MonitorHeadersHandler(final HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.addExchangeCompleteListener(new HeaderMetricsListener()
                                                    .setEventBus(eventBus)
                                                    .setLogger(logger));
        next.handleRequest(exchange);
    }

    public MonitorHeadersHandler setEventBus(IEventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    public MonitorHeadersHandler setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

}

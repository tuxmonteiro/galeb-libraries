/*
 *
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

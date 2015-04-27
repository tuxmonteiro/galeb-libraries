/*
 *
 */

package io.galeb.undertow.handlers;


import io.galeb.core.eventbus.IEventBus;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class MonitorHeadersHandler implements HttpHandler {

    private final HttpHandler next;
    private IEventBus eventBus;

    public MonitorHeadersHandler(final HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.addExchangeCompleteListener(new HeaderMetricsListener().setEventBus(eventBus));
        next.handleRequest(exchange);
    }

    public HttpHandler setEventBus(IEventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

}

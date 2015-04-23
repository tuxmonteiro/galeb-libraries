/*
 *
 */

package io.galeb.undertow.handlers;


import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class HostMetricsHandler implements HttpHandler {

    private final HttpHandler next;
    private final ExchangeCompletionListener exchangeCompletionListener;

    public HostMetricsHandler(final HttpHandler next) {
        this.next = next;
        exchangeCompletionListener = new MetricCompletionListener();
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.addExchangeCompleteListener(exchangeCompletionListener);
        next.handleRequest(exchange);
    }

}

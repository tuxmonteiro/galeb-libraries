package io.galeb.undertow.handlers;

import io.galeb.undertow.fork.server.HttpHandler;
import io.galeb.undertow.fork.server.HttpServerExchange;

public class InternalServerErrorHandler implements HttpHandler {

    private final HttpHandler next;

    public InternalServerErrorHandler(final HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.addExchangeCompleteListener(new CloseConnectionIfNecessaryListener());
        next.handleRequest(exchange);
    }
}

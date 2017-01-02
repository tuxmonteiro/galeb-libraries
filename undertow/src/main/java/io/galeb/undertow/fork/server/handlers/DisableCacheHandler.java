package io.galeb.undertow.fork.server.handlers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.galeb.undertow.fork.server.HandlerWrapper;
import io.galeb.undertow.fork.server.HttpHandler;
import io.galeb.undertow.fork.server.HttpServerExchange;
import io.galeb.undertow.fork.server.handlers.builder.HandlerBuilder;
import io.galeb.undertow.fork.util.Headers;

/**
 *
 * Handler that disables response caching by browsers and proxies.
 *
 *
 * @author Stuart Douglas
 */
public class DisableCacheHandler implements HttpHandler {

    private final HttpHandler next;

    public DisableCacheHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().add(Headers.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        exchange.getResponseHeaders().add(Headers.PRAGMA, "no-cache");
        exchange.getResponseHeaders().add(Headers.EXPIRES, "0");
        next.handleRequest(exchange);
    }

    public static class Builder implements HandlerBuilder {

        @Override
        public String name() {
            return "disable-cache";
        }

        @Override
        public Map<String, Class<?>> parameters() {
            return Collections.emptyMap();
        }

        @Override
        public Set<String> requiredParameters() {
            return Collections.emptySet();
        }

        @Override
        public String defaultParameter() {
            return null;
        }

        @Override
        public HandlerWrapper build(Map<String, Object> config) {
            return new Wrapper();
        }

    }

    private static class Wrapper implements HandlerWrapper {
        @Override
        public HttpHandler wrap(HttpHandler handler) {
            return new DisableCacheHandler(handler);
        }
    }
}

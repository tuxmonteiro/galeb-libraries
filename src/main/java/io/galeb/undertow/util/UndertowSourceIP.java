package io.galeb.undertow.util;

import io.galeb.core.util.SourceIP;
import io.undertow.server.HttpServerExchange;

public class UndertowSourceIP implements SourceIP {

    private HttpServerExchange exchange;

    @Override
    public String getRealSourceIP() {
        // Morpheus: What is real? How do you define 'real'?

        if (exchange == null) {
            return DEFAULT_SOURCE_IP;
        }

        String sourceIP = exchange.getRequestHeaders().getFirst(HTTP_HEADER_XREAL_IP);
        if (sourceIP!=null) {
            return sourceIP;
        }

        sourceIP = exchange.getRequestHeaders().getFirst(HTTP_HEADER_X_FORWARDED_FOR);
        if (sourceIP!=null) {
            return sourceIP.split(",")[0];
        }

        sourceIP = exchange.getSourceAddress().getHostString();
        if (sourceIP!=null) {
            return sourceIP;
        }

        return DEFAULT_SOURCE_IP;
    }

    @Override
    public SourceIP pullFrom(final Object extractable) {
        if (extractable instanceof HttpServerExchange) {
            exchange = (HttpServerExchange) extractable;
        }
        return this;
    }

}

package io.galeb.undertow.util;

import io.galeb.core.util.SourceIP;
import io.undertow.server.HttpServerExchange;

public class UndertowSourceIP implements SourceIP {

    private HttpServerExchange exchange;


    private String getDefaultSourceIP() {
        // Sorry. I'm schizophrenic
        return "127.0.0.1";
    }

    @Override
    public String get() {
        // Morpheus: What is real? How do you define 'real'?

        if (exchange == null) {
            return getDefaultSourceIP();
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

        return getDefaultSourceIP();
    }

    @Override
    public SourceIP pullFrom(final Object extractable) {
        if (extractable instanceof HttpServerExchange) {
            exchange = (HttpServerExchange) extractable;
        }
        return this;
    }

}

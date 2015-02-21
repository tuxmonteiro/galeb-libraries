package com.openvraas.undertow.util;

import io.undertow.server.HttpServerExchange;

import com.openvraas.core.util.SourceIP;

public class UndertowSourceIP implements SourceIP {

    private final HttpServerExchange exchange;

    public UndertowSourceIP(final HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String get() {
        // Morpheus: What is real? How do you define 'real'?

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

        // Sorry. I'm schizophrenic
        return "127.0.0.1";
    }

}

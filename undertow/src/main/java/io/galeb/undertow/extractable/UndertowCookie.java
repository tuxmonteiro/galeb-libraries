package io.galeb.undertow.extractable;

import java.util.UUID;

import io.galeb.core.extractable.RequestCookie;
import io.galeb.core.loadbalance.hash.ExtractableKey;
import io.galeb.fork.undertow.server.HttpServerExchange;
import io.galeb.fork.undertow.server.handlers.Cookie;

public class UndertowCookie implements RequestCookie {

    private String fromAttrib = UUID.randomUUID().toString();

    @Override
    public ExtractableKey from(String from) {
        fromAttrib = from;
        return this;
    }

    @Override
    public String get(final Object extractable) {
        if (extractable instanceof HttpServerExchange) {
            Cookie cookie = ((HttpServerExchange)extractable).getRequestCookies().get(fromAttrib);
            if (cookie!=null) {
                return cookie.getValue();
            }
        }
        return DEFAULT_COOKIE;
    }
}

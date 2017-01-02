package io.galeb.undertow.extractable;

import java.util.EnumSet;

import io.galeb.core.extractable.RequestURI;
import io.galeb.core.loadbalance.hash.ExtractableKey;
import io.galeb.fork.undertow.server.HttpServerExchange;

public class UndertowURI implements RequestURI {

    private String from = DEFAULT_CRITERION;

    boolean isCriterionValid(String from) {
        return EnumSet.allOf(UriCriterion.class).stream()
                .map(UriCriterion::toString)
                .filter(crit -> crit.equals(from))
                .count() == 1;
    }

    @Override
    public ExtractableKey from(String from) {
        if (isCriterionValid(from)) {
            this.from = from;
        }
        return this;
    }

    @Override
    public String get(final Object extractable) {
        if (extractable instanceof HttpServerExchange) {
            String uri = ((HttpServerExchange)extractable).getRelativePath();
            if (from.equals(UriCriterion.FULL.toString())) {
                uri += ((HttpServerExchange)extractable).getQueryString();
            }
            return uri;
        }
        return DEFAULT_URI;
    }

}

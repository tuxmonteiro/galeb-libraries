/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.galeb.fork.undertow.server.handlers;

import java.io.IOException;

import io.galeb.fork.undertow.UndertowLogger;
import io.galeb.fork.undertow.io.IoCallback;
import io.galeb.fork.undertow.io.Sender;
import io.galeb.fork.undertow.predicate.Predicate;
import io.galeb.fork.undertow.predicate.Predicates;
import io.galeb.fork.undertow.server.protocol.http.HttpContinue;
import io.galeb.fork.undertow.server.HttpHandler;
import io.galeb.fork.undertow.server.HttpServerExchange;

/**
 * Handler that provides support for HTTP/1.1 continue responses.
 * <p>
 * If the provided predicate returns <code>true</code> then the request will be
 * accepted, otherwise it will be rejected.
 *
 * If no predicate is supplied then all requests will be accepted.
 *
 * @see io.undertow.server.protocol.http.HttpContinue
 * @author Stuart Douglas
 */
public class HttpContinueAcceptingHandler implements HttpHandler {

    private final HttpHandler next;
    private final Predicate accept;

    public HttpContinueAcceptingHandler(HttpHandler next, Predicate accept) {
        this.next = next;
        this.accept = accept;
    }

    public HttpContinueAcceptingHandler(HttpHandler next) {
        this(next, Predicates.truePredicate());
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if(HttpContinue.requiresContinueResponse(exchange)) {
            if(accept.resolve(exchange)) {
                HttpContinue.sendContinueResponse(exchange, new IoCallback() {
                    @Override
                    public void onComplete(final HttpServerExchange exchange, final Sender sender) {
                        exchange.dispatch(next);
                    }

                    @Override
                    public void onException(final HttpServerExchange exchange, final Sender sender, final IOException exception) {
                        UndertowLogger.REQUEST_IO_LOGGER.ioException(exception);
                        exchange.endExchange();
                    }
                });

            } else {
                HttpContinue.rejectExchange(exchange);
            }
        } else {
            next.handleRequest(exchange);
        }
    }
}

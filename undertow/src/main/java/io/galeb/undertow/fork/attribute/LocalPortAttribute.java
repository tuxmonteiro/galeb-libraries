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

package io.galeb.undertow.fork.attribute;

import java.net.InetSocketAddress;

import io.galeb.undertow.fork.server.HttpServerExchange;

/**
 * The local port
 *
 * @author Stuart Douglas
 */
public class LocalPortAttribute implements ExchangeAttribute {

    public static final String LOCAL_PORT_SHORT = "%p";
    public static final String LOCAL_PORT = "%{LOCAL_PORT}";

    public static final ExchangeAttribute INSTANCE = new LocalPortAttribute();

    private LocalPortAttribute() {

    }

    @Override
    public String readAttribute(final HttpServerExchange exchange) {
        InetSocketAddress localAddress = (InetSocketAddress) exchange.getConnection().getLocalAddress();
        return Integer.toString(localAddress.getPort());
    }

    @Override
    public void writeAttribute(final HttpServerExchange exchange, final String newValue) throws ReadOnlyAttributeException {
        throw new ReadOnlyAttributeException("Local port", newValue);
    }

    public static final class Builder implements ExchangeAttributeBuilder {

        @Override
        public String name() {
            return "Local Port";
        }

        @Override
        public ExchangeAttribute build(final String token) {
            if (token.equals(LOCAL_PORT) || token.equals(LOCAL_PORT_SHORT)) {
                return LocalPortAttribute.INSTANCE;
            }
            return null;
        }

        @Override
        public int priority() {
            return 0;
        }
    }
}
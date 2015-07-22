/*
 * Copyright (c) 2014-2015 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.undertow.handlers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import jodd.util.Wildcard;

public class PathGlobHandler implements HttpHandler {

    private final HttpHandler defaultHandler;
    private final Map<String, HttpHandler> patterns = new LinkedHashMap<>();

    public PathGlobHandler(final HttpHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public PathGlobHandler() {
        this(ResponseCodeHandler.HANDLE_404);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (patterns.isEmpty()) {
            defaultHandler.handleRequest(exchange);
            return;
        }
        final String path = exchange.getRelativePath();

        AtomicBoolean hit = new AtomicBoolean(false);
        patterns.entrySet().stream().forEach(entry -> {
            hit.set(Wildcard.match(path, entry.getKey()));
            if (hit.get()) {
                try {
                    entry.getValue().handleRequest(exchange);
                } catch (Exception e) {
                    // TODO: log.error(e)
                }
            }
        });
        if (!hit.get()) {
            defaultHandler.handleRequest(exchange);
        }
    }

    public synchronized PathGlobHandler addPattern(final String pattern, final HttpHandler handler) {
        patterns.put(pattern, handler);
        return this;
    }

    public synchronized PathGlobHandler removePattern(final String pattern) {
        patterns.remove(pattern);
        return this;
    }

    public synchronized PathGlobHandler clearPattern() {
        patterns.clear();
        return this;
    }

    public HttpHandler getDefaultHandler() {
        return defaultHandler;
    }
}

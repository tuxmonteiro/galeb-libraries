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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import io.galeb.core.model.Rule;
import io.galeb.fork.undertow.server.HttpHandler;
import io.galeb.fork.undertow.server.HttpServerExchange;
import io.galeb.fork.undertow.server.handlers.ResponseCodeHandler;
import io.galeb.fork.undertow.util.*;
import jodd.util.Wildcard;

public class PathGlobHandler implements HttpHandler {

    private HttpHandler defaultHandler = ResponseCodeHandler.HANDLE_500;
    private final Map<Rule, HttpHandler> rules = new CopyOnWriteMap<>();

    private Comparator<Map.Entry<Rule, HttpHandler>> ruleOrderComparator() {
        return (e1, e2) -> Integer.compare(e1.getKey().getRuleOrder(), e2.getKey().getRuleOrder());
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (rules.isEmpty()) {
            defaultHandler.handleRequest(exchange);
            return;
        }
        final String path = exchange.getRelativePath();

        AtomicBoolean hit = new AtomicBoolean(false);
        rules.entrySet().stream().sorted(ruleOrderComparator()).forEach(entry -> {
            if (!hit.get()) {
                String pathKey = entry.getKey().getMatch();
                if (pathKey.endsWith("/") && !pathKey.contains("*")) {
                    pathKey = pathKey + "*";
                }
                hit.set(Wildcard.match(path, pathKey));
                if (hit.get()) {
                    try {
                        entry.getValue().handleRequest(exchange);
                    } catch (Exception e) {
                        // TODO: log.error(e)
                    }
                }
            }
        });
        if (!hit.get()) {
            defaultHandler.handleRequest(exchange);
        }
    }

    public synchronized boolean contains(final Rule rule) {
        return rules.containsKey(rule);
    }

    public synchronized PathGlobHandler addRule(final Rule rule, final HttpHandler handler) {
        rules.put(rule, handler);
        return this;
    }

    public synchronized PathGlobHandler removeRule(final Rule rule) {
        rules.remove(rule);
        return this;
    }

    public HttpHandler getDefaultHandler() {
        return defaultHandler;
    }

    public HttpHandler setDefaultHandler(HttpHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }
}

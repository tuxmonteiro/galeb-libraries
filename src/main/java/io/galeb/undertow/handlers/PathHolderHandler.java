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

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.cache.LRUCache;
import io.undertow.util.PathMatcher;

public class PathHolderHandler implements HttpHandler {

    private final PathMatcher<HttpHandler> pathMatcher = new PathMatcher<>();

    private final LRUCache<String, PathMatcher.PathMatch<HttpHandler>> cache;

    public PathHolderHandler(final HttpHandler defaultHandler) {
        this(0);
        pathMatcher.addPrefixPath("/", defaultHandler);
    }

    public PathHolderHandler(final HttpHandler defaultHandler, int cacheSize) {
        this(cacheSize);
        pathMatcher.addPrefixPath("/", defaultHandler);
    }

    public PathHolderHandler() {
        this(0);
    }

    public PathHolderHandler(int cacheSize) {
        if(cacheSize > 0) {
            cache = new LRUCache<>(cacheSize, -1);
        } else {
            cache = null;
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        PathMatcher.PathMatch<HttpHandler> match = null;
        boolean hit = false;
        if(cache != null) {
            match = cache.get(exchange.getRelativePath());
            hit = true;
        }
        if(match == null) {
            match = pathMatcher.match(exchange.getRelativePath());
        }
        if (match.getValue() == null) {
            ResponseCodeHandler.HANDLE_404.handleRequest(exchange);
            return;
        }
        if(hit) {
            cache.add(exchange.getRelativePath(), match);
        }
        match.getValue().handleRequest(exchange);
    }

    public synchronized PathHolderHandler addPrefixPath(final String path, final HttpHandler handler) {
        Handlers.handlerNotNull(handler);
        pathMatcher.addPrefixPath(path, handler);
        return this;
    }

    public synchronized PathHolderHandler addExactPath(final String path, final HttpHandler handler) {
        Handlers.handlerNotNull(handler);
        pathMatcher.addExactPath(path, handler);
        return this;
    }

    public synchronized PathHolderHandler removePrefixPath(final String path) {
        pathMatcher.removePrefixPath(path);
        return this;
    }

    public synchronized PathHolderHandler removeExactPath(final String path) {
        pathMatcher.removeExactPath(path);
        return this;
    }

    public synchronized PathHolderHandler clearPaths() {
        pathMatcher.clearPaths();
        return this;
    }
}

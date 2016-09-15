/*
 * Copyright (c) 2014-2016 Globo.com - ATeam
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

package io.galeb.undertow.server;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import org.xnio.Options;

import java.util.HashMap;
import java.util.Map;

class BuilderFactory {

    private static final int DEFAULT_IO_THREADS   = 4;
    private static final int DEFAULT_NUM_WORKS    = Runtime.getRuntime().availableProcessors() * 8;
    private static final int DEFAULT_BACKLOG      = 1000;
    private static final int DEFAULT_IDLE_TIMEOUT = 0;

    private final Map<String, String> options = new HashMap<>();

    private int    port = 8080;
    private String host = "0.0.0.0";

    public BuilderFactory setHost(String host) {
        this.host = host;
        return this;
    }

    public BuilderFactory setPort(int port) {
        this.port = port;
        return this;
    }

    public BuilderFactory setOptions(final Map<String, String> options) {
        this.options.putAll(options);
        return this;
    }

    public BuilderFactory clearOptions() {
        this.options.clear();
        return this;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public Builder getBuilder() {

        final int iothreads = options.containsKey("IoThreads") ? Integer.parseInt(options.get("IoThreads")) : DEFAULT_IO_THREADS;
        final int works = options.containsKey("workers") ? Integer.parseInt(options.get("workers")) : DEFAULT_NUM_WORKS;
        final int maxWorks = options.containsKey("max_workers") ? Integer.parseInt(options.get("max_workers")) : works;
        final int backlog = options.containsKey("backlog") ? Integer.parseInt(options.get("backlog")) : DEFAULT_BACKLOG;
        final int idleTimeout = options.containsKey("idleTimeout") ? Integer.parseInt(options.get("idleTimeout")) : DEFAULT_IDLE_TIMEOUT;

        return Undertow.builder().addHttpListener(port, host)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setServerOption(UndertowOptions.IDLE_TIMEOUT, idleTimeout)
                .setIoThreads(iothreads)
                .setWorkerThreads(works)
                .setWorkerOption(Options.WORKER_TASK_MAX_THREADS, maxWorks)
                .setSocketOption(Options.BACKLOG, backlog);
    }
}

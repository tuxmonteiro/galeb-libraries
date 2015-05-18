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

package io.galeb.undertow.router;

import io.galeb.core.model.Farm;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;

import java.util.HashMap;
import java.util.Map;

public class RouterApplication {

    private int port = 8080;
    private String host = "0.0.0.0";
    private final Map<String, String> options = new HashMap<>();
    private Farm farm = null;

    public RouterApplication setHost(String host) {
        this.host = host;
        return this;
    }

    public RouterApplication setPort(int port) {
        this.port = port;
        return this;
    }

    public RouterApplication setOptions(Map<String, String> options) {
        this.options.putAll(options);
        return this;
    }

    public RouterApplication setFarm(Farm farm) {
        this.farm = farm;
        farm.setOptions(options);
        return this;
    }

    public void start() {
        if (farm==null) {
            return;
        }

        final Object rootHandlerObj = farm.getRootHandler();
        HttpHandler rootHandler = null;

        if (rootHandlerObj instanceof HttpHandler) {
            rootHandler = (HttpHandler) rootHandlerObj;
        } else {
            return;
        }
        final int iothreads = options.containsKey("IoThreads") ? Integer.parseInt(options.get("IoThreads")) : 4;

        final Undertow router = Undertow.builder().addHttpListener(port, host)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setIoThreads(iothreads)
                .setHandler(rootHandler)
                .build();

        router.start();

    }

}

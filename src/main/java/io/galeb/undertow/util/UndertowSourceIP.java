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

package io.galeb.undertow.util;

import io.galeb.core.util.Constants;
import io.galeb.core.util.SourceIP;
import io.undertow.server.HttpServerExchange;

public class UndertowSourceIP implements SourceIP {

    private final HttpServerExchange exchange;

    public UndertowSourceIP(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getRealSourceIP() {
        // Morpheus: What is real? How do you define 'real'?

        String sourceIP = null;

        if (exchange == null) {
            return DEFAULT_SOURCE_IP;
        }

        if (IGNORE_XFORWARDED_FOR.isPresent() &&
                (!IGNORE_XFORWARDED_FOR.get().equalsIgnoreCase(Constants.FALSE) ||
                 !IGNORE_XFORWARDED_FOR.get().equals("0"))) {
            sourceIP = exchange.getSourceAddress().getHostString();
        } else {
            sourceIP = exchange.getRequestHeaders().getFirst(HTTP_HEADER_XREAL_IP);
            if (sourceIP!=null) {
                return sourceIP;
            }
            sourceIP = exchange.getRequestHeaders().getFirst(HTTP_HEADER_X_FORWARDED_FOR);
            if (sourceIP!=null) {
                return sourceIP.split(",")[0];
            }
            sourceIP = exchange.getSourceAddress().getHostString();
        }
        if (sourceIP!=null) {
            return sourceIP;
        }

        return DEFAULT_SOURCE_IP;
    }

}

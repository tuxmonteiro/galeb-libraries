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

package io.galeb.undertow.extractable;

import io.galeb.core.extractable.SourceIP;
import io.galeb.fork.undertow.server.HttpServerExchange;

public class UndertowSourceIP implements SourceIP {

    private String extractSourceIP(final HttpServerExchange exchange) {

        String aSourceIP = null;

        if (exchange == null) {
            return DEFAULT_SOURCE_IP;
        }

        if (IGNORE_XFORWARDED_FOR.isPresent() &&
                (!IGNORE_XFORWARDED_FOR.get().equalsIgnoreCase(Boolean.toString(false)) ||
                 !IGNORE_XFORWARDED_FOR.get().equals("0"))) {
            aSourceIP = exchange.getSourceAddress().getHostString();
        } else {
            aSourceIP = exchange.getRequestHeaders().getFirst(HTTP_HEADER_XREAL_IP);
            if (aSourceIP!=null) {
                return aSourceIP;
            }
            aSourceIP = exchange.getRequestHeaders().getFirst(HTTP_HEADER_X_FORWARDED_FOR);
            if (aSourceIP!=null) {
                return aSourceIP.split(",")[0];
            }
            aSourceIP = exchange.getSourceAddress().getHostString();
        }
        if (aSourceIP!=null) {
            return aSourceIP;
        }

        return DEFAULT_SOURCE_IP;
    }

    @Override
    public String get(Object extractable) {
        if (extractable instanceof HttpServerExchange) {
            return extractSourceIP((HttpServerExchange) extractable);
        }
        return DEFAULT_SOURCE_IP;
    }

}

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

import io.galeb.core.statsd.NullStatsdClient;
import io.galeb.core.statsd.StatsdClient;
import io.undertow.attribute.ResponseTimeAttribute;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

class HeaderMetricsListener implements ExchangeCompletionListener, ProcessorLocalStatusCode {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ResponseTimeAttribute responseTimeAttribute = new ResponseTimeAttribute(TimeUnit.SECONDS);

    private StatsdClient statsdClient = new NullStatsdClient();
    private int maxRequestTime = Integer.MAX_VALUE - 1;
    private boolean forceChangeStatus = false;

    @Override
    public void exchangeEvent(final HttpServerExchange exchange, final NextListener nextListener) {
        final String realDest = exchange.getAttachment(BackendSelector.REAL_DEST);
        String virtualhost = exchange.getHostName();
        String backend = realDest != null ? realDest : "UNKNOWN@" + virtualhost;
        int statusCode = exchange.getStatusCode();
        long responseBytesSent = exchange.getResponseBytesSent();
        final HttpString method = exchange.getRequestMethod();
        final Integer responseTime = Integer.valueOf(responseTimeAttribute.readAttribute(exchange));
        int fakeStatusCode = getFakeStatusCode(realDest, statusCode, responseBytesSent, responseTime, maxRequestTime);
        int statusCodeLogged = statusCode;
        if (fakeStatusCode != NOT_MODIFIED) {
            statusCodeLogged = fakeStatusCode - ProcessorLocalStatusCode.OFFSET_LOCAL_ERROR;
            if (statusCodeLogged != statusCode) {
                exchange.setStatusCode(statusCodeLogged);
            }
            statusCode = fakeStatusCode;
        }
        String httpStatus = String.valueOf(HttpStatus.SC_OK);
        if (forceChangeStatus || statusCodeLogged != HttpStatus.SC_BAD_GATEWAY) {
            httpStatus = String.valueOf(statusCode);
        }
        sendHttpStatusCount(virtualhost, backend, httpStatus);
        sendRequestTime(virtualhost, backend, responseTime);
        if (method != null) {
            sendHttpMethodCount(virtualhost, backend, method.toString());
        }

        nextListener.proceed();
    }

    public HeaderMetricsListener setMaxRequestTime(int maxRequestTime) {
        this.maxRequestTime = maxRequestTime;
        return this;
    }

    public HeaderMetricsListener forceChangeStatus(boolean forceChangeStatus) {
        this.forceChangeStatus = forceChangeStatus;
        return this;
    }


    public HeaderMetricsListener setStatsd(StatsdClient statsdClient) {
        this.statsdClient = statsdClient;
        return this;
    }

    private void sendHttpMethodCount(String virtualhostId, String backendId, String method) {
        final String virtualhost = StatsdClient.cleanUpKey(virtualhostId);
        final String backend = StatsdClient.cleanUpKey(backendId);
        final String key = virtualhost + StatsdClient.STATSD_SEP + backend + StatsdClient.STATSD_SEP + StatsdClient.PROP_METHOD_PREFIX + StatsdClient.STATSD_SEP + method;
        statsdClient.incr(key);
    }

    private void sendHttpStatusCount(String virtualhostId, String backendId, String httpStatus) {
        final String virtualhost = StatsdClient.cleanUpKey(virtualhostId);
        final String backend = StatsdClient.cleanUpKey(backendId);
        final String key = virtualhost + StatsdClient.STATSD_SEP + backend + StatsdClient.STATSD_SEP + StatsdClient.PROP_HTTPCODE_PREFIX+httpStatus;
        statsdClient.incr(key);
    }


    private void sendRequestTime(String virtualhostId, String backendId, long requestTime) {
        final String virtualhost = StatsdClient.cleanUpKey(virtualhostId);
        final String backend = StatsdClient.cleanUpKey(backendId);
        final String key = virtualhost + StatsdClient.STATSD_SEP + backend + StatsdClient.STATSD_SEP + StatsdClient.PROP_REQUESTTIME;
        statsdClient.timing(key, requestTime);
    }

}

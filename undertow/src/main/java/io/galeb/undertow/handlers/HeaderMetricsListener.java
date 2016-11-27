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
import io.galeb.core.util.Constants;
import io.undertow.attribute.ResponseTimeAttribute;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.galeb.core.statsd.StatsdClient.PROP_HTTPCODE_PREFIX;
import static io.galeb.core.statsd.StatsdClient.PROP_METHOD_PREFIX;
import static io.galeb.core.statsd.StatsdClient.PROP_REQUESTTIME;
import static io.galeb.core.statsd.StatsdClient.STATSD_SEP;

class HeaderMetricsListener implements ExchangeCompletionListener, ProcessorLocalStatusCode {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CLUSTER_ID_PREFIX = "_";

    private final ResponseTimeAttribute responseTimeAttribute = new ResponseTimeAttribute(TimeUnit.MILLISECONDS);

    private StatsdClient statsdClient = new NullStatsdClient();
    private int maxRequestTime = Integer.MAX_VALUE - 1;
    private boolean forceChangeStatus = false;
    private final String clusterId = System.getProperty(Constants.SysProp.PROP_CLUSTER_ID.toString(),
                                                        Constants.SysProp.PROP_CLUSTER_ID.def())
                                            .replaceAll("[-_](blue|green)$","");

    @Override
    public void exchangeEvent(final HttpServerExchange exchange, final NextListener nextListener) {
        try {
            final String realDest = exchange.getAttachment(BackendSelector.REAL_DEST);
            String virtualhost = exchange.getHostName();
            String backend = realDest != null ? realDest : "UNKNOWN@" + virtualhost;
            int statusCode = exchange.getStatusCode();
            long responseBytesSent = exchange.getResponseBytesSent();
            final HttpString method = exchange.getRequestMethod();
            final Integer responseTime = getResponseTime(exchange);
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
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            nextListener.proceed();
        }
    }

    public int getResponseTime(HttpServerExchange exchange) {
        return Math.round(Float.parseFloat(responseTimeAttribute.readAttribute(exchange)));
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

    private void sendHttpMethodCount(final String virtualhostId, final String backendId, final String method) {
        final String virtualhost = StatsdClient.cleanUpKey(virtualhostId);
        final String backend = StatsdClient.cleanUpKey(backendId);
        final Stream<String> keys = Stream.of(
                virtualhost + STATSD_SEP + backend + STATSD_SEP + PROP_METHOD_PREFIX + STATSD_SEP + method,
                virtualhost + STATSD_SEP + PROP_METHOD_PREFIX + STATSD_SEP + method,
                CLUSTER_ID_PREFIX + clusterId + STATSD_SEP + PROP_METHOD_PREFIX + STATSD_SEP + method);
        keys.forEach(k -> statsdClient.incr(k));
    }

    private void sendHttpStatusCount(String virtualhostId, String backendId, String httpStatus) {
        final String virtualhost = StatsdClient.cleanUpKey(virtualhostId);
        final String backend = StatsdClient.cleanUpKey(backendId);
        final Stream<String> keys = Stream.of(
                virtualhost + STATSD_SEP + backend + STATSD_SEP + PROP_HTTPCODE_PREFIX + httpStatus,
                virtualhost + STATSD_SEP + PROP_HTTPCODE_PREFIX + httpStatus,
                CLUSTER_ID_PREFIX + clusterId + STATSD_SEP + PROP_HTTPCODE_PREFIX + STATSD_SEP + httpStatus);
        keys.forEach(k -> statsdClient.incr(k));
    }

    private void sendRequestTime(String virtualhostId, String backendId, long requestTime) {
        final String virtualhost = StatsdClient.cleanUpKey(virtualhostId);
        final String backend = StatsdClient.cleanUpKey(backendId);
        final Stream<String> keys = Stream.of(
                virtualhost + STATSD_SEP + backend + STATSD_SEP + PROP_REQUESTTIME,
                virtualhost + STATSD_SEP + PROP_REQUESTTIME,
                CLUSTER_ID_PREFIX + clusterId + STATSD_SEP + PROP_REQUESTTIME);
        keys.forEach(k -> statsdClient.timing(k, requestTime));
    }

}

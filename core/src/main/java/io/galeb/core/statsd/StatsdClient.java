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

package io.galeb.core.statsd;

public interface StatsdClient {

    String STATSD_HOST   = "host";
    String STATSD_PORT   = "port";
    String STATSD_PREFIX = "prefix";

    String STATSD_SEP    = ".";

    String PREFIX = StatsdClient.class.getPackage().getName();

    static void setup() {
        if (System.getProperty(PREFIX + STATSD_SEP + STATSD_HOST)==null) {
            System.setProperty(PREFIX + STATSD_SEP + STATSD_HOST, "127.0.0.1");
        }
        if (System.getProperty(PREFIX + STATSD_SEP + STATSD_PORT)==null) {
            System.setProperty(PREFIX + STATSD_SEP + STATSD_PORT, "8125");
        }
        if (System.getProperty(PREFIX + STATSD_SEP + STATSD_PREFIX)==null) {
            System.setProperty(PREFIX + STATSD_SEP + STATSD_PREFIX, "galeb");
        }
    }

    static String getHost() {
        setup();
        return System.getProperty(PREFIX + STATSD_SEP + STATSD_HOST);
    }

    static String getPort() {
        setup();
        return System.getProperty(PREFIX + STATSD_SEP + STATSD_PORT);
    }

    static String getPrefix() {
        setup();
        return System.getProperty(PREFIX + STATSD_SEP + STATSD_PREFIX);
    }

    static String cleanUpKey(String key) {
        return key.replaceAll("http://", "").replaceAll("[.:]", "_");
    }

    String PROP_STATUSCODE = "status";
    String PROP_HTTPCODE_PREFIX = "httpCode";
    String PROP_METHOD_PREFIX = "httpMethod";
    String PROP_REQUESTTIME = "requestTime";
    String PROP_REQUESTTIME_AVG = "requestTimeAvg";

    default StatsdClient host(String server) {
        return this;
    }

    default StatsdClient port(int port) {
        return this;
    }

    default StatsdClient prefix(String prefix) {
        return this;
    }

    default void incr(String metricName) {
        // default
    }

    default void incr(String metricName, int step) {
        // default
    }

    default void incr(String metricName, int step, double rate) {
        // default
    }

    default void incr(String metricName, double rate) {
        // default
    }

    default void decr(String metricName) {
        // default
    }

    default void decr(String metricName, int step) {
        // default
    }

    default void decr(String metricName, int step, double rate) {
        // default
    }

    default void decr(String metricName, double rate) {
        // default
    }

    default void count(String metricName, int value) {
        // default
    }

    default void count(String metricName, int value, double rate) {
        // default
    }

    default void gauge(String metricName, double value) {
        // default
    }

    default void gauge(String metricName, double value, double rate) {
        // default
    }

    default void set(String metricName, String value) {
        // default
    }

    default void set(String metricName, String value, double rate) {
        // default
    }

    default void timing(String metricName, long value) {
        // default
    }

    default void timing(String metricName, long value, double rate) {
        // default
    }

}

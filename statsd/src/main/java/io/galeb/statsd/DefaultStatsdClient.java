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

package io.galeb.statsd;

import io.galeb.core.statsd.StatsdClient;
import io.galeb.core.statsd.annotation.StatsdClientSingletoneProducer;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import com.timgroup.statsd.NonBlockingStatsDClient;

@Default
@Singleton
public class DefaultStatsdClient implements StatsdClient {

    private static final StatsdClient INSTANCE = new DefaultStatsdClient();

    private final NonBlockingStatsDClient client =
            new NonBlockingStatsDClient(StatsdClient.getPrefix(), StatsdClient.getHost(), Integer.valueOf(StatsdClient.getPort()));

    private DefaultStatsdClient() {
        // SINGLETON
    }

    @Produces @StatsdClientSingletoneProducer
    public static StatsdClient getInstance() {
        return INSTANCE;
    }

    @Override
    public void incr(String metricName, int step, double rate) {
        count(metricName, step, rate);
    }

    @Override
    public void incr(String metricName) {
        incr(metricName, 1, 1.0);
    }

    @Override
    public void incr(String metricName, int step) {
        incr(metricName, step, 1.0);
    }

    @Override
    public void incr(String metricName, double rate) {
        incr(metricName, 1, rate);
    }

    @Override
    public void decr(String metricName, int step, double rate) {
        client.count(metricName, -1*step, rate);
    }

    @Override
    public void decr(String metricName) {
        decr(metricName, 1, 1.0);
    }

    @Override
    public void decr(String metricName, int step) {
        decr(metricName, step, 1.0);
    }

    @Override
    public void decr(String metricName, double rate) {
        decr(metricName, 1, rate);
    }

    @Override
    public void count(String metricName, int value, double rate) {
        client.count(metricName, value, rate);
    }

    @Override
    public void count(String metricName, int value) {
        count(metricName, value, 1.0);
    }

    @Override
    public void gauge(String metricName, double value, double rate) {
        client.recordGaugeValue(metricName, value);
    }

    @Override
    public void gauge(String metricName, double value) {
        gauge(metricName, value, 1.0);
    }

    @Override
    public void set(String metricName, String value, double rate) {
        client.recordSetEvent(metricName, value);
    }

    @Override
    public void set(String metricName, String value) {
        set(metricName, value, 1.0);
    }

    @Override
    public void timing(String metricName, long value, double rate) {
        client.recordExecutionTime(metricName, value, rate);
    }

    @Override
    public void timing(String metricName, long value) {
        timing(metricName, value, 1.0);
    }
}

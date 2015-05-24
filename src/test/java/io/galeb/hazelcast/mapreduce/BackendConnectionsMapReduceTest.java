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

package io.galeb.hazelcast.mapreduce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import io.galeb.core.logging.Logger;
import io.galeb.core.mapreduce.MapReduce;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BackendConnectionsMapReduceTest {

    private static HazelcastInstance hzInstance;

    private final Logger log = mock(Logger.class);

    private MapReduce mapReduce;
    String metricKey = "TEST";

    @BeforeClass
    public static void setUpStatic() {
        final Config config = new Config();
        config.getGroupConfig().setName(UUID.randomUUID().toString());
        final NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().setMembers(Arrays.asList(new String[]{"127.0.0.1"}));
        hzInstance = Hazelcast.newHazelcastInstance(config);
    }

    @Before
    public void setUp() {
        doNothing().when(log).error(any(Throwable.class));
        doNothing().when(log).debug(any(Throwable.class));
        mapReduce = new BackendConnectionsMapReduce(hzInstance, log);
    }

    @Test
    public void addMetricsTest() {

        mapReduce.addMetrics(metricKey, 0);

        assertThat(mapReduce.contains(metricKey)).isTrue();
    }

    @Test
    public void checkBackendWithTimeout() throws InterruptedException {
        final long timeout = 1L; // MILLISECOND (Hint: 0L = TTL Forever)

        mapReduce.setTimeOut(timeout).addMetrics(metricKey, 0);
        Thread.sleep(timeout+1);

        assertThat(mapReduce.contains(metricKey)).isFalse();
    }

    @Test
    public void checkLocalMapReduceResult() {
        final int numConn = 10;

        mapReduce.addMetrics(metricKey, numConn);
        final int connections = mapReduce.reduce().get(metricKey);

        assertThat(connections).isEqualTo(numConn);
    }

}

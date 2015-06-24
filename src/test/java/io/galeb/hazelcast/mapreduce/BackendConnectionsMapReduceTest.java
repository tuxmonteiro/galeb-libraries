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
import io.galeb.hazelcast.NodeLifecycleListener;

import org.junit.Before;
import org.junit.Test;

public class BackendConnectionsMapReduceTest {

    private final Logger log = mock(Logger.class);

    private MapReduce mapReduce;
    String metricKey = "TEST";

    @Before
    public void setUp() {
        doNothing().when(log).error(any(Throwable.class));
        doNothing().when(log).debug(any(Throwable.class));
        mapReduce = new BackendConnectionsMapReduce();
    }

    @Test
    public void addMetricsTest() {

        mapReduce.put(metricKey, 0);

        assertThat(mapReduce.contains(metricKey)).isTrue();
    }

    @Test
    public void checkBackendWithTimeout() throws InterruptedException {
        final long timeout = 1L; // MILLISECOND (Hint: 0L = TTL Forever)

        mapReduce.setTimeOut(timeout).put(metricKey, 0);
        Thread.sleep(timeout+1);

        assertThat(mapReduce.contains(metricKey)).isFalse();
    }

    @Test
    public void checkLocalMapReduceResult() {
        final int numConn = 10;

        mapReduce.put(metricKey, numConn);
        NodeLifecycleListener.forceReady();
        final int connections = mapReduce.reduce().get(metricKey);

        assertThat(connections).isEqualTo(numConn);
    }

}

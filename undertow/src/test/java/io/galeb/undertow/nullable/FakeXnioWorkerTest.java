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

package io.galeb.undertow.nullable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.xnio.XnioWorker;

public class FakeXnioWorkerTest {

    private final XnioWorker nullXnioWorker = FakeXnioWorker.NULL;

    @Test
    public void shutdownNowTest() {
        assertThat(nullXnioWorker.shutdownNow()).isEmpty();
    }

    @Test
    public void isShutdownTest() {
        assertThat(nullXnioWorker.isShutdown()).isFalse();
    }

    @Test
    public void isTerminatedTest() {
        assertThat(nullXnioWorker.isTerminated()).isFalse();
    }

    @Test
    public void awaitTerminationTest() throws InterruptedException {
        assertThat(nullXnioWorker.awaitTermination(0, null)).isFalse();
    }

    @Test
    public void getIoThreadCountTest() {
        assertThat(nullXnioWorker.getIoThreadCount()).isEqualTo(0);
    }

}

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
import java.io.IOException;

import org.junit.Test;
import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.xnio.conduits.StreamSinkConduit;

public class FakeStreamSinkConduitTest {

    private final StreamSinkConduit nullStreamSinkConduit = FakeStreamSinkConduit.NULL;

    @Test
    public void isWriteShutdownTest() {
        assertThat(nullStreamSinkConduit.isWriteShutdown()).isFalse();
    }

    @Test
    public void isWriteResumedTest() {
        assertThat(nullStreamSinkConduit.isWriteResumed()).isFalse();
    }

    @Test
    public void getWriteThreadTest() {
        assertThat(nullStreamSinkConduit.getWriteThread()).isInstanceOf(XnioIoThread.class);
    }

    @Test
    public void getWorkerTest() throws Exception {
        assertThat(nullStreamSinkConduit.getWorker()).isInstanceOf(XnioWorker.class);
    }

    @Test
    public void transferFromWithFileChannelTest() throws IOException {
        assertThat(nullStreamSinkConduit.transferFrom(null, 0, 0)).isEqualTo(0);
    }

    @Test
    public void transferFromWithStreamSourceChannelTest() throws IOException {
        assertThat(nullStreamSinkConduit.transferFrom(null, 0, null)).isEqualTo(0);
    }

    @Test
    public void write1Test() throws Exception {
        assertThat(nullStreamSinkConduit.write(null)).isEqualTo(0);
    }

    @Test
    public void write2Test() throws Exception {
        assertThat(nullStreamSinkConduit.write(null, 0, 0)).isEqualTo(0);
    }

    @Test
    public void writeFinal1Test() throws Exception {
        assertThat(nullStreamSinkConduit.writeFinal(null)).isEqualTo(0);
    }

    @Test
    public void writeFinal2Test() throws Exception {
        assertThat(nullStreamSinkConduit.writeFinal(null, 0, 0)).isEqualTo(0);
    }
}

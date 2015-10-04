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
import org.xnio.XnioExecutor.Key;
import org.xnio.XnioIoThread;

public class FakeXnioIoThreadTest {

    private final XnioIoThread nullFakeXnioIoThread = FakeXnioIoThread.NULL;

    @Test
    public void executeAfterTest() {
        assertThat(nullFakeXnioIoThread.executeAfter(null, 0, null)).isInstanceOf(Key.class);
    }

    @Test
    public void executeAtIntervalTest() {
        assertThat(nullFakeXnioIoThread.executeAtInterval(null, 0, null)).isInstanceOf(Key.class);
    }

}

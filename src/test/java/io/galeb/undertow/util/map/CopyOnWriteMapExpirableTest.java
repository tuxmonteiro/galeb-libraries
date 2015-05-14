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

package io.galeb.undertow.util.map;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CopyOnWriteMapExpirableTest {

    private static final long DEFAULT_TTL = 500L;

    private Map<String, Integer> copyOnWriteMapExpirable;

    @Before
    public void setUp() {
        copyOnWriteMapExpirable = new CopyOnWriteMapExpirable<>(DEFAULT_TTL, TimeUnit.MILLISECONDS);
    }

    @After
    public void cleanUp() {
        copyOnWriteMapExpirable = null;
    }

    @Test
    public void putAndGetTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assertThat(copyOnWriteMapExpirable.get(toString())).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    public void sizeTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assertThat(copyOnWriteMapExpirable.size()).isEqualTo(1);
    }

    @Test
    public void isEmptyTest() {
        assertThat(copyOnWriteMapExpirable).isEmpty();
    }

    @Test
    public void containsKeyTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assertThat(copyOnWriteMapExpirable).containsKey(toString());
    }

    @Test
    public void containsValueTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assertThat(copyOnWriteMapExpirable).containsValue(Integer.MIN_VALUE);
    }

    @Test
    public void removeTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assertThat(copyOnWriteMapExpirable).containsValue(Integer.MIN_VALUE);
        copyOnWriteMapExpirable.remove(toString());
        assertThat(copyOnWriteMapExpirable).isEmpty();
    }

    @Test
    public void putAllTest() {
        Map<String, Integer> mapTemp = new HashMap<>();
        for (int x=0; x<10; x++) {
            mapTemp.put(Integer.toString(x), x);
        }
        copyOnWriteMapExpirable.putAll(mapTemp);
        for (int x=0; x<10; x++) {
            assertThat(copyOnWriteMapExpirable.get(Integer.toString(x))).isEqualTo(x);
        }
    }

    @Test
    public void clearTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        copyOnWriteMapExpirable.clear();
        assertThat(copyOnWriteMapExpirable).isEmpty();
    }

    @Test
    public void keySetTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        assertThat(copyOnWriteMapExpirable.keySet()).hasSize(10);
    }

    @Test
    public void valuesTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        assertThat(copyOnWriteMapExpirable.values()).hasSize(10);
    }

    @Test
    public void entrySetTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        assertThat(copyOnWriteMapExpirable.entrySet()).hasSize(10);
    }

    @Test
    public void clearExpiredTest() throws InterruptedException {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        Thread.sleep(DEFAULT_TTL);
        ((CopyOnWriteMapExpirable<String, Integer>) copyOnWriteMapExpirable).clearExpired();
        assertThat(copyOnWriteMapExpirable).isEmpty();
    }

    @Test
    public void renewAllTest() throws InterruptedException {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        Thread.sleep(DEFAULT_TTL);
        ((CopyOnWriteMapExpirable<String, Integer>) copyOnWriteMapExpirable).renewAll();
        ((CopyOnWriteMapExpirable<String, Integer>) copyOnWriteMapExpirable).clearExpired();
        assertThat(copyOnWriteMapExpirable.entrySet()).hasSize(10);
    }

}

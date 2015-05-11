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

import java.util.concurrent.TimeUnit;

import org.xnio.XnioIoThread;

public class FakeXnioIoThread {

    private FakeXnioIoThread() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final XnioIoThread NULL = new XnioIoThread(null, 0) {

        @Override
        public void execute(Runnable command) {
            // NULL
        }

        @Override
        public Key executeAfter(
                Runnable command,
                long time,
                TimeUnit unit) {

            return new Key() {
                @Override
                public boolean remove() {
                    return false;
                }
            };
        }

        @Override
        public Key executeAtInterval(
                Runnable command,
                long time,
                TimeUnit unit) {

            return new Key() {
                @Override
                public boolean remove() {
                    return false;
                }
            };
        }
    };
}

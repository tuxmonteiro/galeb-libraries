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

import java.nio.ByteBuffer;

import org.xnio.Pooled;

public class FakePolled {

    private FakePolled() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final Pooled<ByteBuffer> NULL = new Pooled<ByteBuffer>() {

        @Override
        public void discard() {
            // NULL
        }

        @Override
        public void free() {
            // NULL
        }

        @Override
        public ByteBuffer getResource() {
            return ByteBuffer.allocate(0);
        }

        @Override
        public void close() {
            // NULL
        }
    };
}

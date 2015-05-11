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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.xnio.channels.StreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;
import org.xnio.conduits.WriteReadyHandler;

public class FakeStreamSinkConduit {

    private FakeStreamSinkConduit() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final StreamSinkConduit NULL = new StreamSinkConduit() {

        @Override
        public void terminateWrites() throws IOException {
            // NULL
        }

        @Override
        public boolean isWriteShutdown() {
            return false;
        }

        @Override
        public void resumeWrites() {
            // NULL
        }

        @Override
        public void suspendWrites() {
            // NULL
        }

        @Override
        public void wakeupWrites() {
            // NULL
        }

        @Override
        public boolean isWriteResumed() {
            return false;
        }

        @Override
        public void awaitWritable() {
            // NULL
        }

        @Override
        public void awaitWritable(long time, TimeUnit timeUnit) {
            // NULL
        }

        @Override
        public XnioIoThread getWriteThread() {
            return FakeXnioIoThread.NULL;
        }

        @Override
        public void setWriteReadyHandler(WriteReadyHandler handler) {
            // NULL
        }

        @Override
        public void truncateWrites() throws IOException {
            // NULL
        }

        @Override
        public boolean flush() throws IOException {
            return false;
        }

        @Override
        public XnioWorker getWorker() {
            return FakeXnioWorker.NULL;
        }

        @Override
        public long transferFrom(FileChannel src, long position, long count) {
            return 0L;
        }

        @Override
        public long transferFrom(
                StreamSourceChannel source,
                long count,
                ByteBuffer throughBuffer) {
            return 0L;
        }

        @Override
        public int write(ByteBuffer src) {
            return 0;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offs, int len) {
            return 0L;
        }

        @Override
        public int writeFinal(ByteBuffer src) {
            return 0;
        }

        @Override
        public long writeFinal(ByteBuffer[] srcs, int offset, int length) {
            return 0L;
        }
    };
}

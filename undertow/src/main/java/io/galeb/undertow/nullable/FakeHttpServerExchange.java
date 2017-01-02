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

import io.galeb.fork.undertow.connector.ByteBufferPool;
import io.galeb.fork.undertow.connector.PooledByteBuffer;
import io.galeb.fork.undertow.server.HttpServerExchange;
import io.galeb.fork.undertow.server.HttpUpgradeListener;
import io.galeb.fork.undertow.server.SSLSessionInfo;
import io.galeb.fork.undertow.server.ServerConnection;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.xnio.ChannelListener.Setter;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.Pool;
import org.xnio.Pooled;
import org.xnio.StreamConnection;
import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.xnio.channels.ConnectedChannel;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;

public class FakeHttpServerExchange {

    private FakeHttpServerExchange() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final HttpServerExchange NULL = new HttpServerExchange(new ServerConnection() {

        @Override
        public Setter<? extends ConnectedChannel> getCloseSetter() {
            return FakeSetter.NULL;
        }

        @Override
        public Pool<ByteBuffer> getBufferPool() {
            return new Pool<ByteBuffer>() {
                @Override
                public Pooled<ByteBuffer> allocate() {
                    return FakePolled.NULL;
                }
            };
        }

        @Override
        public XnioWorker getWorker() {
            return FakeXnioWorker.NULL;
        }

        @Override
        public XnioIoThread getIoThread() {
            return FakeXnioIoThread.NULL;
        }

        @Override
        public HttpServerExchange sendOutOfBandResponse(
                HttpServerExchange exchange) {
            return exchange;
        }

        @Override
        public boolean isContinueResponseSupported() {
            return false;
        }

        @Override
        public void terminateRequestChannel(HttpServerExchange exchange) {
            // NULL
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean supportsOption(Option<?> option) {
            return false;
        }

        @Override
        public <T> T getOption(Option<T> option) {
            return null;
        }

        @Override
        public <T> T setOption(Option<T> option, T value) {
            return null;
        }

        @Override
        public void close() {
            // NULL
        }

        @SuppressWarnings("serial")
        @Override
        public SocketAddress getPeerAddress() {
            return new SocketAddress() {
            };
        }

        @SuppressWarnings({ "serial", "unchecked" })
        @Override
        public <A extends SocketAddress> A getPeerAddress(Class<A> type) {
            return (A) new SocketAddress() {
            };
        }

        @SuppressWarnings("serial")
        @Override
        public SocketAddress getLocalAddress() {
            return new SocketAddress() {
            };
        }

        @SuppressWarnings({ "serial", "unchecked" })
        @Override
        public <A extends SocketAddress> A getLocalAddress(Class<A> type) {
            return (A) new SocketAddress() {
            };
        }

        @Override
        public OptionMap getUndertowOptions() {
            return OptionMap.EMPTY;
        }

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public SSLSessionInfo getSslSessionInfo() {
            return FakeSSLSessionInfo.NULL;
        }

        @Override
        public void setSslSessionInfo(SSLSessionInfo sessionInfo) {
            // NULL
        }

        @Override
        public void addCloseListener(CloseListener listener) {
            // NULL
        }

        @Override
        protected StreamConnection upgradeChannel() {
            return null;
        }

        @Override
        protected ConduitStreamSinkChannel getSinkChannel() {
            return null;
        }

        @Override
        protected ConduitStreamSourceChannel getSourceChannel() {
            return null;
        }

        @Override
        protected StreamSinkConduit getSinkConduit(HttpServerExchange exchange,
                StreamSinkConduit conduit) {
            return FakeStreamSinkConduit.NULL;
        }

        @Override
        protected boolean isUpgradeSupported() {
            return false;
        }

        @Override
        protected boolean isConnectSupported() {
            return false;
        }

        @Override
        protected void exchangeComplete(HttpServerExchange exchange) {
            // NULL
        }

        @Override
        protected void setUpgradeListener(HttpUpgradeListener upgradeListener) {
            // NULL
        }

        @Override
        protected void setConnectListener(HttpUpgradeListener connectListener) {
            // NULL
        }

        @Override
        protected void maxEntitySizeUpdated(HttpServerExchange exchange) {
            // NULL
        }

        @Override
        public String getTransportProtocol() {
            return null;
        }

        @Override
        public ByteBufferPool getByteBufferPool() {
            return new ByteBufferPool() {
                @Override
                public PooledByteBuffer allocate() {
                    return null;
                }

                @Override
                public void close() {

                }

                @Override
                public int getBufferSize() {
                    return 0;
                }
            };
        }

    });
}

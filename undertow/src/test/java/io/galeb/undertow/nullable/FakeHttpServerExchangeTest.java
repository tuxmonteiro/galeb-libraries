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
import java.net.SocketAddress;

import io.undertow.server.SSLSessionInfo;
import io.undertow.server.ServerConnection;

import org.xnio.ChannelListener.Setter;
import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xnio.Pool;

public class FakeHttpServerExchangeTest {

    private ServerConnection serverConnection;

    @Before
    public void setUp() {
        serverConnection = FakeHttpServerExchange.NULL.getConnection();
    }

    @After
    public void cleanDown() {
        serverConnection = null;
    }

    @Test
    public void serverConnectionGetCloseSetterTest() {
        assertThat(serverConnection.getCloseSetter()).isInstanceOf(Setter.class);
    }

    @Test
    public void serverConnectionGetBufferPoolTest() {
        assertThat(serverConnection.getBufferPool()).isInstanceOf(Pool.class);
    }

    @Test
    public void serverConnectionGetWorkerTest() {
        assertThat(serverConnection.getWorker()).isInstanceOf(XnioWorker.class);
    }

    @Test
    public void serverConnectionGetIoThreadTest() throws IOException {
        assertThat(serverConnection.getIoThread()).isInstanceOf(XnioIoThread.class);
    }

    @Test
    public void serverConnectionSendOutOfBandResponseTest() throws IOException {
        assertThat(serverConnection.sendOutOfBandResponse(null)).isNull();
    }

    @Test
    public void serverConnectionIsContinueResponseSupportedTest() {
        assertThat(serverConnection.isContinueResponseSupported()).isFalse();
    }

    @Test
    public void serverConnectionIsOpenTest() {
        assertThat(serverConnection.isOpen()).isFalse();
    }

    @Test
    public void serverConnectionSupportsOptionTest() {
        assertThat(serverConnection.supportsOption(null)).isFalse();
    }

    @Test
    public void serverConnectionGetOptionTest() throws IOException {
        assertNull(serverConnection.getOption(null));
    }

    @Test
    public void serverConnectionSetOptionTest() throws IllegalArgumentException, IOException {
        assertNull(serverConnection.setOption(null, null));
    }

    @Test
    public void serverConnectionGetPeerAddressTest() {
        assertThat(serverConnection.getPeerAddress()).isInstanceOf(SocketAddress.class);
    }

    @Test
    public void serverConnectionGetLocalAddressTest() {
        assertThat(serverConnection.getLocalAddress()).isInstanceOf(SocketAddress.class);
    }

    @Test
    public void serverConnectionGetSslSessionInfoTest() {
        assertThat(serverConnection.getSslSessionInfo()).isInstanceOf(SSLSessionInfo.class);
    }

    @Test
    public void serverConnectionGetTransportProtocolTest() {
        assertThat(serverConnection.getTransportProtocol()).isNull();
    }

}

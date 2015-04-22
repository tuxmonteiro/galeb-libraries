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

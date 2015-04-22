package io.galeb.undertow.nullable;

import static org.assertj.core.api.Assertions.assertThat;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.junit.Test;

import io.undertow.server.RenegotiationRequiredException;
import io.undertow.server.SSLSessionInfo;

public class FakeSSLSessionInfoTest {

    private final SSLSessionInfo nullSSLSessionInfo = FakeSSLSessionInfo.NULL;

    @Test
    public void getSessionIdTest() {
        assertThat(nullSSLSessionInfo.getSessionId()).hasSize(0);
    }

    @Test
    public void getCipherSuiteTest() {
        assertThat(nullSSLSessionInfo.getCipherSuite()).isEmpty();
    }

    @Test
    public void getPeerCertificatesTest()
            throws SSLPeerUnverifiedException, RenegotiationRequiredException {
        assertThat(nullSSLSessionInfo.getPeerCertificates()).hasSize(0);
    }

    @Test
    public void getPeerCertificateChainTest()
            throws SSLPeerUnverifiedException, RenegotiationRequiredException {
        assertThat(nullSSLSessionInfo.getPeerCertificateChain()).hasSize(0);
    }

}

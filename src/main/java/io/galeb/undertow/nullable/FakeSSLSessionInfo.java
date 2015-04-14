package io.galeb.undertow.nullable;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.RenegotiationRequiredException;
import io.undertow.server.SSLSessionInfo;

import java.io.IOException;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;

import org.xnio.SslClientAuthMode;

public class FakeSSLSessionInfo {

    private FakeSSLSessionInfo() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final SSLSessionInfo NULL = new SSLSessionInfo() {

        @Override
        public byte[] getSessionId() {
            return new byte[0];
        }

        @Override
        public String getCipherSuite() {
            return "";
        }

        @Override
        public Certificate[] getPeerCertificates()
                throws SSLPeerUnverifiedException,
                RenegotiationRequiredException {
            return new Certificate[0];
        }

        @Override
        public X509Certificate[] getPeerCertificateChain()
                throws SSLPeerUnverifiedException,
                RenegotiationRequiredException {
            return new X509Certificate[0];
        }

        @Override
        public void renegotiate(HttpServerExchange exchange,
                SslClientAuthMode sslClientAuthMode) throws IOException {
        }
    };
}

package io.galeb.undertow.nullable;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.SSLSessionInfo;

import java.security.cert.Certificate;

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
        public Certificate[] getPeerCertificates() {
            return new Certificate[0];
        }

        @Override
        public X509Certificate[] getPeerCertificateChain() {
            return new X509Certificate[0];
        }

        @Override
        public void renegotiate(HttpServerExchange exchange,
                SslClientAuthMode sslClientAuthMode) {
            // NULL
        }
    };
}

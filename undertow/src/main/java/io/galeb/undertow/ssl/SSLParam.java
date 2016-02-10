package io.galeb.undertow.ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

public class SSLParam {

    private static SSLParameters sslParameters = null;

    private SSLParam() {
        // static service only
    }

    public static synchronized void setSslParameters(final SSLParameters sslParameters) {
        SSLParam.sslParameters = sslParameters;
    }

    public static SSLEngine apply(SSLEngine sslEngine) {
        final SSLEngine engine = sslEngine;
        if (sslParameters != null) {
//            SSLParameters originalSslParams = engine.getSSLParameters();
            engine.setSSLParameters(sslParameters);
//            engine.setSSLParameters(originalSslParams);
        }
        return engine;
    }
}

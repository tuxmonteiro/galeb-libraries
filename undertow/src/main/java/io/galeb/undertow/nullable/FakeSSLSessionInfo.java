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

import java.security.cert.Certificate;

import javax.security.cert.X509Certificate;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.SSLSessionInfo;
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

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

import javax.net.ssl.SSLPeerUnverifiedException;

import io.undertow.server.RenegotiationRequiredException;
import io.undertow.server.SSLSessionInfo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

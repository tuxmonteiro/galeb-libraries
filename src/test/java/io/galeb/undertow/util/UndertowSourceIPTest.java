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

package io.galeb.undertow.util;

import static io.galeb.core.util.SourceIP.DEFAULT_SOURCE_IP;
import static org.assertj.core.api.Assertions.assertThat;
import io.galeb.core.util.SourceIP;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UndertowSourceIPTest {

    private static final String FAKE_IP = "127.0.0.2";

    private UndertowSourceIP undertowSourceIP;

    private  HttpServerExchange fakeExchange;


    @Before
    public void setUp() {
        fakeExchange = new HttpServerExchange(null);
        fakeExchange.setSourceAddress(new InetSocketAddress(DEFAULT_SOURCE_IP, 0));
        undertowSourceIP = new UndertowSourceIP(fakeExchange);
    }

    @After
    public void clean() {
        undertowSourceIP = null;
        fakeExchange = null;
    }

    @Test
    public void defaultSourceIPTest() {
        assertThat(undertowSourceIP.getRealSourceIP())
            .containsIgnoringCase(SourceIP.DEFAULT_SOURCE_IP);
    }

    @Test
    public void withHeaderXRealIPTest() {

        fakeExchange.getRequestHeaders()
            .addFirst(new HttpString(SourceIP.HTTP_HEADER_XREAL_IP), FAKE_IP);

        assertThat(undertowSourceIP.getRealSourceIP())
            .containsIgnoringCase(FAKE_IP);
    }

    @Test
    public void withHeaderXForwardedForTest() {
        fakeExchange.getRequestHeaders()
            .addFirst(new HttpString(SourceIP.HTTP_HEADER_X_FORWARDED_FOR), FAKE_IP);

        assertThat(undertowSourceIP.getRealSourceIP())
            .containsIgnoringCase(FAKE_IP);
    }

    @Test
    public void sourceAddressTest() {
        fakeExchange.setSourceAddress(new InetSocketAddress(FAKE_IP, 0));

        assertThat(undertowSourceIP.getRealSourceIP())
            .containsIgnoringCase(FAKE_IP);
    }

}

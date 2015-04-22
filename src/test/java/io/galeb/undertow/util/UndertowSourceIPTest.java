package io.galeb.undertow.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;

import io.galeb.core.util.SourceIP;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UndertowSourceIPTest {

    private static final String FAKE_IP = "127.0.0.2";

    private UndertowSourceIP undertowSourceIP;

    private  HttpServerExchange fakeExchange;


    @Before
    public void setUp() {
        undertowSourceIP = new UndertowSourceIP();
        fakeExchange = new HttpServerExchange(null);
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

        assertThat(undertowSourceIP.pullFrom(fakeExchange).getRealSourceIP())
            .containsIgnoringCase(FAKE_IP);
    }

    @Test
    public void withHeaderXForwardedForTest() {
        fakeExchange.getRequestHeaders()
        .addFirst(new HttpString(SourceIP.HTTP_HEADER_X_FORWARDED_FOR), FAKE_IP);

        assertThat(undertowSourceIP.pullFrom(fakeExchange).getRealSourceIP())
            .containsIgnoringCase(FAKE_IP);
    }

    @Test
    public void sourceAddressTest() {
        fakeExchange.setSourceAddress(new InetSocketAddress(FAKE_IP, 0));

        assertThat(undertowSourceIP.pullFrom(fakeExchange).getRealSourceIP())
            .containsIgnoringCase(FAKE_IP);
    }

    @Test
    public void defaultSourceIPIfExtractableIsNullTest() {
        fakeExchange = null;

        assertThat(undertowSourceIP.pullFrom(fakeExchange).getRealSourceIP())
            .containsIgnoringCase(SourceIP.DEFAULT_SOURCE_IP);
    }


}

package io.galeb.undertow.handlers;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.Host;

public class BackendSelectorTest {

    private BackendSelector backendSelector;

    private Host[] hosts;

    private final Map<String, Object> params = new HashMap<>();

    @Before
    public void setUp() {
        backendSelector = new BackendSelector();
        hosts = new Host[3];
    }

    @After
    public void clean() {
        backendSelector = null;
        params.clear();
    }

    @Test
    public void selectHostNotNullTest() {
        assertThat(backendSelector.selectHost(hosts)).isNotNull();
    }

    @Test
    public void selectHostDefaultIsOneTest() {
        assertThat(backendSelector.selectHost(hosts)).isEqualTo(1);
    }

    @Test
    public void traceAddHeaderXProxyHostTest() {
        final HttpServerExchange fakeExchange = new HttpServerExchange(null);
        backendSelector.setExchange(fakeExchange).selectHost(hosts);
        assertThat(fakeExchange.getRequestHeaders().getFirst(BackendSelector.X_PROXY_HOST)).isNotEmpty();
    }

    @Test
    public void traceWithHostNullTest() {
        final HttpServerExchange fakeExchange = new HttpServerExchange(null);
        backendSelector.setExchange(fakeExchange).selectHost(hosts);
        assertThat(fakeExchange.getRequestHeaders().getFirst(BackendSelector.X_PROXY_HOST)).containsIgnoringCase("UNDEF");
    }

    @Test
    public void traceAddHeaderXStartTimeTest() {
        final HttpServerExchange fakeExchange = new HttpServerExchange(null);
        backendSelector.setExchange(fakeExchange).selectHost(hosts);
        assertThat(fakeExchange.getRequestHeaders().getFirst(BackendSelector.X_START_TIME)).isNotEmpty();
    }

    @Test
    public void newParamsIsCopyThenIsNotSameInstanceTest() {
        final Map<String, Object> paramsOld = new HashMap<>();
        final Map<String, Object> paramsNew = new HashMap<>();

        backendSelector.setParams(paramsOld);
        backendSelector.setParams(paramsNew);

        assertThat(paramsOld).isNotSameAs(paramsNew);
    }

}

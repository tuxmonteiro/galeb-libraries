package io.galeb.undertow.handlers;


import static org.assertj.core.api.Assertions.assertThat;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient.Host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class BackendSelectorTest {

    private BackendSelector backendSelector;

    private final List<Host> hosts = new ArrayList<>();

    private final Map<String, Object> params = new HashMap<>();

    @Before
    public void setUp() {
        backendSelector = new BackendSelector();
        for (int x=0;x<10;x++){
//            hosts.add(new Host(null, null, new URI("http://" + Integer.toString(x)), null, null));
        }
    }

    @After
    public void clean() {
        backendSelector = null;
        params.clear();
    }

    @Test
    public void selectHostNotNullTest() {
        assertThat(backendSelector.selectHost((Host[]) hosts.toArray())).isNotNull();
    }

    @Test
    public void selectHostDefaultIsOneTest() {
        assertThat(backendSelector.selectHost((Host[]) hosts.toArray())).isEqualTo(1);
    }

    @Test
    public void traceAddHeaderXProxyHostTest() {
        final HttpServerExchange fakeExchange = new HttpServerExchange(null);
        backendSelector.setExchange(fakeExchange).selectHost((Host[]) hosts.toArray());
        assertThat(fakeExchange.getRequestHeaders().getFirst(BackendSelector.X_PROXY_HOST)).isNotEmpty();
    }

    @Test
    public void traceWithHostNullTest() {
        final HttpServerExchange fakeExchange = new HttpServerExchange(null);
        backendSelector.setExchange(fakeExchange).selectHost((Host[]) hosts.toArray());
        assertThat(fakeExchange.getRequestHeaders().getFirst(BackendSelector.X_PROXY_HOST)).containsIgnoringCase("UNDEF");
    }

    @Test
    public void traceAddHeaderXStartTimeTest() {
        final HttpServerExchange fakeExchange = new HttpServerExchange(null);
        backendSelector.setExchange(fakeExchange).selectHost((Host[]) hosts.toArray());
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

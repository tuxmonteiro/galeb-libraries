package io.galeb.undertow.handlers;

import io.galeb.fork.undertow.server.Connectors;
import io.galeb.fork.undertow.server.HttpServerExchange;
import io.galeb.fork.undertow.server.ServerConnection;
import org.junit.Test;
import org.springframework.util.Assert;

import static org.mockito.Mockito.mock;

public class HeaderMetricsListenerTest {

    @Test
    public void responseTimeAttribute() {
        long sleepInMillis = 1000;
        HttpServerExchange exchange = new HttpServerExchange(mock(ServerConnection.class));
        Connectors.setRequestStartTime(exchange);
        HeaderMetricsListener headerMetricsListener = new HeaderMetricsListener();
        try {
            Thread.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int responseTime = headerMetricsListener.getResponseTime(exchange);

        Assert.isTrue(responseTime > sleepInMillis);
    }
}

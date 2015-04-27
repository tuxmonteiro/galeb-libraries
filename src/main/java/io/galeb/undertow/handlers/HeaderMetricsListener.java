package io.galeb.undertow.handlers;

import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Metrics;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import javax.inject.Inject;

class HeaderMetricsListener implements ExchangeCompletionListener {

    @Inject
    private Logger logger;

    private IEventBus eventBus = IEventBus.NULL;

    @Override
    public void exchangeEvent(final HttpServerExchange exchange, final NextListener nextListener) {
        try {

            final HeaderValues headerXProxyHost = exchange.getRequestHeaders().get("X-Proxy-Host");
            if (headerXProxyHost==null) {
                return;
            }
            final Metrics metrics = new Metrics();
            metrics.setParentId(exchange.getHostName());
            metrics.setId(headerXProxyHost.getFirst());
            metrics.getProperties().put("status", exchange.getResponseCode());
            final HeaderValues headerXStartTime = exchange.getRequestHeaders().get("X-Start-Time");
            if (headerXStartTime!=null) {
                metrics.getProperties().put("requestTime", System.nanoTime()/1000000 - Long.valueOf(headerXStartTime.getFirst())/1000000);
            }
            eventBus.sendMetrics(metrics);
        } finally {
            nextListener.proceed();
        }
    }

    public ExchangeCompletionListener setEventBus(IEventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

}
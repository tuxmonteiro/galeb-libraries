package io.galeb.undertow.handlers;

import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Metrics;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

class HeaderMetricsListener implements ExchangeCompletionListener {

    private Logger logger;
    private IEventBus eventBus;

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
            metrics.getProperties().put(Metrics.PROP_STATUSCODE, exchange.getResponseCode());
            final HeaderValues headerXStartTime = exchange.getRequestHeaders().get("X-Start-Time");
            if (headerXStartTime!=null) {
                metrics.getProperties().put(Metrics.PROP_REQUESTTIME, System.nanoTime()/1000000 - Long.valueOf(headerXStartTime.getFirst())/1000000);
            }
            eventBus.onRequestMetrics(metrics);
        } catch (final RuntimeException e) {
            logger.error(e);
        } finally {
            nextListener.proceed();
        }
    }

    public HeaderMetricsListener setEventBus(IEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.getQueueManager();
        return this;
    }

    public HeaderMetricsListener setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

}
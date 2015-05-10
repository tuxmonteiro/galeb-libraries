package io.galeb.undertow.handlers;

import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Metrics;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import java.util.NoSuchElementException;

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
            metrics.putProperty(Metrics.PROP_STATUSCODE, exchange.getResponseCode());
            final HeaderValues headerXStartTime = exchange.getRequestHeaders().get("X-Start-Time");
            if (headerXStartTime!=null) {
                String headerXStartTimeStr = headerXStartTime.getFirst();
                if (!"".equals(headerXStartTimeStr)) {
                    metrics.putProperty(Metrics.PROP_REQUESTTIME, (System.nanoTime() - Long.valueOf(headerXStartTimeStr))/1000000);
                } else {
                    metrics.putProperty(Metrics.PROP_REQUESTTIME, 0L);
                }
            }
            eventBus.onRequestMetrics(metrics);
        } catch (NoSuchElementException e1) {
            logger.debug(e1);
        } catch (final RuntimeException e2) {
            logger.error(e2);
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
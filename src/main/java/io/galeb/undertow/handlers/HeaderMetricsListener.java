package io.galeb.undertow.handlers;

import io.galeb.core.logging.Logger;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import javax.inject.Inject;

class HeaderMetricsListener implements ExchangeCompletionListener {

    @Inject
    private Logger logger;

    @Override
    public void exchangeEvent(final HttpServerExchange exchange, final NextListener nextListener) {
        try {

            final HeaderValues headerXProxyHost = exchange.getRequestHeaders().get("X-Proxy-Host");
            if (headerXProxyHost==null) {
                return;
            }
            showMetric("Virtual Host", exchange.getHostName());
            showMetric("Real Host", headerXProxyHost.getFirst());
            showMetric("Http Status", exchange.getResponseCode());
            final HeaderValues headerXStartTime = exchange.getRequestHeaders().get("X-Start-Time");
            if (headerXStartTime!=null) {
                showMetric("Request Time", System.nanoTime()/1000000 - Long.valueOf(headerXStartTime.getFirst())/1000000);
            }

        } finally {
            nextListener.proceed();
        }
    }

    private void showMetric(String metric, String dataMetric) {
        logger.debug(String.format("[%s] %s: %s", this, metric, dataMetric));
    }

    private void showMetric(String metric, int dataMetric) {
        logger.debug(String.format("[%s] %s: %d", this, metric, dataMetric));
    }

    private void showMetric(String metric, long dataMetric) {
        logger.debug(String.format("[%s] %s: %d", this, metric, dataMetric));
    }

    @SuppressWarnings("unused")
    private void showMetric(String metric, double dataMetric) {
        logger.debug(String.format("[%s] %s: %f", this, metric, dataMetric));
    }

}
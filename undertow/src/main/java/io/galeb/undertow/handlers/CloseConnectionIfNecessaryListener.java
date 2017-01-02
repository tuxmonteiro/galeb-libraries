package io.galeb.undertow.handlers;

import io.galeb.fork.undertow.server.ExchangeCompletionListener;
import io.galeb.fork.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xnio.IoUtils;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

public class CloseConnectionIfNecessaryListener implements ExchangeCompletionListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        try {
            int statusCode = exchange.getStatusCode();
            if (statusCode >= SC_INTERNAL_SERVER_ERROR) {
                LOGGER.error("Calling CloseConnectionIfNecessaryListener: statusCode = " + Integer.toString(statusCode));
                IoUtils.safeClose(exchange.getConnection());
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        finally {
            nextListener.proceed();
        }
    }
}

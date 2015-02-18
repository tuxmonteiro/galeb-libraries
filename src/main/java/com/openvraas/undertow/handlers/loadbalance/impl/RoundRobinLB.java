package com.openvraas.undertow.handlers.loadbalance.impl;

import io.undertow.server.HttpServerExchange;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.openvraas.undertow.handlers.loadbalance.LoadBalanceCriterion;

public class RoundRobinLB extends LoadBalanceCriterion {

    private final AtomicInteger currentHost = new AtomicInteger(0);

    @Override
    public int getLastChoice() {
        return currentHost.get();
    }

    @Override
    public int getChoice(final Object[] hosts) {
        return currentHost.incrementAndGet() % hosts.length;
    }

    @Override
    public synchronized void reset() {
        currentHost.lazySet(0);
    }

    @Override
    public LoadBalanceCriterion setParams(Map<String, Object> params,
            HttpServerExchange exchange) {
        return this;
    }

}

package com.openvraas.undertow.handlers.loadbalance.impl;

import io.undertow.server.HttpServerExchange;

import java.util.Map;

import com.openvraas.undertow.handlers.loadbalance.AbstractLoadBalancePolicy;

public class RandomPolicy extends AbstractLoadBalancePolicy {

    public int lastChosen = 0;

    @Override
    public int getLastChoice() {
        return lastChosen;
    }

    @Override
    public int getChoice(Object[] hosts) {
        int chosen = (int) (Math.random() * (hosts.length - Float.MIN_VALUE));
;       lastChosen = chosen;
        return chosen;
    }

    @Override
    public void reset() {
    }

    @Override
    public AbstractLoadBalancePolicy setParams(Map<String, Object> params,
            HttpServerExchange exchange) {
        return this;
    }

}

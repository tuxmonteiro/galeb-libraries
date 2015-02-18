package com.openvraas.undertow.handlers.loadbalance;

import io.undertow.server.HttpServerExchange;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractLoadBalancePolicy {

    public enum Algorithm {
        ROUNDROBIN("RoundRobinPolicy"),
        RANDOM("RandomPolicy"),
        IPHASH("IPHashPolicy"),
        LEASTCONN("LeastConnPolicy");

        private String algoNameStr = "";

        Algorithm(String algoNameStr) {
            this.algoNameStr = algoNameStr;
        }

        @Override
        public String toString() {
            return algoNameStr;
        }
    }

    /** The Constant ALGORITHM_MAP. */
    private static final Map<String, Algorithm> ALGORITHM_MAP = new HashMap<>();
    static {
        for (Algorithm algorithm : EnumSet.allOf(Algorithm.class)) {
            ALGORITHM_MAP.put(algorithm.toString(), algorithm);
        }
    }

    public static boolean hasLoadBalanceAlgorithm(String algorithmStr) {
        return ALGORITHM_MAP.containsKey(algorithmStr);
    }

    public static String LOADBALANCE_ALGORITHM_NAME_FIELD = "loadBalancePolicy";

    public static Algorithm DEFAULT_ALGORITHM = Algorithm.ROUNDROBIN;

    public static AbstractLoadBalancePolicy NULL = new AbstractLoadBalancePolicy() {
        @Override
        public void reset() {
        }

        @Override
        public int getLastChoice() {
            return 0;
        }

        @Override
        public int getChoice(Object[] hosts) {
            return 0;
        }

        @Override
        public AbstractLoadBalancePolicy setParams(final Map<String, Object> params, final HttpServerExchange exchange) {
            return this;
        }
    };

    public abstract int getLastChoice();

    public abstract int getChoice(final Object[] hosts);

    public abstract void reset();

    public abstract AbstractLoadBalancePolicy setParams(final Map<String, Object> params, final HttpServerExchange exchange);

}

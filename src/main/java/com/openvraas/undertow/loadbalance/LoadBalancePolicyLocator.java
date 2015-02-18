package com.openvraas.undertow.loadbalance;

import static com.openvraas.undertow.loadbalance.AbstractLoadBalancePolicy.Algorithm.*;

import java.util.HashMap;
import java.util.Map;

import com.openvraas.undertow.loadbalance.impl.IPHashPolicy;
import com.openvraas.undertow.loadbalance.impl.RandomPolicy;
import com.openvraas.undertow.loadbalance.impl.RoundRobinPolicy;

public class LoadBalancePolicyLocator {

    Map<String, AbstractLoadBalancePolicy> loadbalanceCriterionMap = new HashMap<>();

    public LoadBalancePolicyLocator() {
        loadbalanceCriterionMap.put(ROUNDROBIN.toString(), new RoundRobinPolicy());
        loadbalanceCriterionMap.put(RANDOM.toString(), new RandomPolicy());
        loadbalanceCriterionMap.put(IPHASH.toString(), new IPHashPolicy());
    }

    public AbstractLoadBalancePolicy get(String loadBalanceAlgorithm) {
        AbstractLoadBalancePolicy abstractLoadBalancePolicy = loadbalanceCriterionMap.get(loadBalanceAlgorithm);
        if (abstractLoadBalancePolicy==null) {
            abstractLoadBalancePolicy = AbstractLoadBalancePolicy.NULL;
        }
        return abstractLoadBalancePolicy;
    }

}

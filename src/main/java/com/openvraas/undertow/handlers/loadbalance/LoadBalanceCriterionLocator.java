package com.openvraas.undertow.handlers.loadbalance;

import static com.openvraas.undertow.handlers.loadbalance.LoadBalanceCriterion.Algorithm.*;

import java.util.HashMap;
import java.util.Map;

import com.openvraas.undertow.handlers.loadbalance.impl.RoundRobinLB;
import com.openvraas.undertow.handlers.loadbalance.impl.RandomLB;
import com.openvraas.undertow.handlers.loadbalance.impl.IPHashLB;

public class LoadBalanceCriterionLocator {

    Map<String, LoadBalanceCriterion> loadbalanceCriterionMap = new HashMap<>();

    public LoadBalanceCriterionLocator() {
        loadbalanceCriterionMap.put(ROUNDROBIN.toString(), new RoundRobinLB());
        loadbalanceCriterionMap.put(RANDOM.toString(), new RandomLB());
        loadbalanceCriterionMap.put(IPHASH.toString(), new IPHashLB());
    }

    public LoadBalanceCriterion get(String loadBalanceAlgorithm) {
        LoadBalanceCriterion loadBalanceCriterion = loadbalanceCriterionMap.get(loadBalanceAlgorithm);
        if (loadBalanceCriterion==null) {
            loadBalanceCriterion = LoadBalanceCriterion.NULL;
        }
        return loadBalanceCriterion;
    }

}

package com.openvraas.undertow.handlers.loadbalance.impl;

import io.undertow.server.HttpServerExchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openvraas.core.util.consistenthash.ConsistentHash;
import com.openvraas.core.util.consistenthash.HashAlgorithm;
import com.openvraas.core.util.consistenthash.HashAlgorithm.HashType;
import com.openvraas.undertow.handlers.loadbalance.AbstractLoadBalancePolicy;

public class IPHashPolicy extends AbstractLoadBalancePolicy {

    public enum Params {
        IPHASH_HASH_ALGORITHM("HashAlgorithm"),
        IPHASH_NUM_REPLICAS("NumReplicas"),
        IPHASH_SOURCE_IP("SourceIP");

        private String paramNameStr = "";

        Params(String paramNameStr) {
            this.paramNameStr = paramNameStr;
        }

        @Override
        public String toString() {
            return paramNameStr;
        }
    }

    private int last = 0;

    private HashAlgorithm hashAlgorithm = new HashAlgorithm(HashType.SIP24);

    private int numReplicas = 1;

    private final ConsistentHash<Integer> consistentHash = new ConsistentHash<Integer>(hashAlgorithm, numReplicas, new ArrayList<Integer>());

    private volatile String sourceIP = "127.0.0.1";

    private AtomicBoolean needRebuild = new AtomicBoolean(true);

    //Useful http headers
    private final String httpHeaderXRealIp         = "X-Real-IP";
    private final String httpHeaderXForwardedFor   = "X-Forwarded-For";
    private final String httpHeaderForwardedFor    = "Forwarded-For";

    @Override
    public int getLastChoice() {
        return last;
    }

    @Override
    public int getChoice(final Object[] hosts) {
        if (needRebuild.get()) {
            Collection<Integer> hostsPos = new LinkedList<Integer>();
            for (int x=0;x<hosts.length;x++) {
                hostsPos.add(x);
            }
            consistentHash.rebuild(hashAlgorithm, numReplicas, hostsPos);
            needRebuild.compareAndSet(true, false);
        }
        return consistentHash.get(sourceIP);
    }

    @Override
    public void reset() {
        needRebuild.set(true);
    }

    @Override
    public AbstractLoadBalancePolicy setParams(final Map<String, Object> params,
            final HttpServerExchange exchange) {
        String hashAlgorithmStr = (String) params.get(Params.IPHASH_HASH_ALGORITHM.toString());
        if (hashAlgorithmStr!=null) {
            if (HashAlgorithm.hashTypeFromString(hashAlgorithmStr)!=null) {
                hashAlgorithm = new HashAlgorithm(hashAlgorithmStr);
            }
        }
        String numReplicaStr = (String) params.get(Params.IPHASH_NUM_REPLICAS.toString());
        if (numReplicaStr!=null) {
            numReplicas = Integer.valueOf(numReplicaStr);
        }
        sourceIP = getRealSourceIP(exchange);
        return this;
    }

    private String getRealSourceIP(final HttpServerExchange exchange) {
        // Morpheus: What is real? How do you define 'real'?

        String sourceIP = exchange.getRequestHeaders().getFirst(httpHeaderXRealIp);
        if (sourceIP!=null) {
            return sourceIP;
        }

        sourceIP = exchange.getRequestHeaders().getFirst(httpHeaderXForwardedFor);
        if (sourceIP!=null) {
            return sourceIP.split(",")[0];
        }

        sourceIP = exchange.getRequestHeaders().getFirst(httpHeaderForwardedFor);
        if (sourceIP!=null) {
            return sourceIP.split(",")[0];
        }

        sourceIP = exchange.getSourceAddress().getHostString();
        if (sourceIP!=null) {
            return sourceIP;
        }

        // Sorry. I'm schizophrenic
        return "127.0.0.1";
    }

}

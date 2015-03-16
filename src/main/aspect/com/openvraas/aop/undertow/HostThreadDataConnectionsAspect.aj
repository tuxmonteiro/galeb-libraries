package com.openvraas.aop.undertow;

import java.util.Map;

import org.aspectj.lang.JoinPoint;

import io.undertow.server.handlers.proxy.ProxyConnectionPool;
import io.undertow.util.CopyOnWriteMap;

public aspect HostThreadDataConnectionsAspect {

    private static final boolean ENABLED = false;

    private volatile String threadId = "UNDEF";
    private final Map<String, Integer> counter = new CopyOnWriteMap<>();
    private final Map<String, Map<String, Integer>> uris = new CopyOnWriteMap<>();

    pointcut myPointcut() : if(ENABLED) && set(* io.undertow.server.handlers.proxy.ProxyConnectionPool.HostThreadData.connections);

    after() : myPointcut() {
        threadId = thisJoinPoint.getTarget().toString();

        if (thisJoinPoint.getThis() instanceof ProxyConnectionPool) {
            showTotal(threadId, thisJoinPoint);
            threadId = "UNDEF";
        }
    }

    private synchronized int showTotal(final String threadId, final JoinPoint joinPoint) {
        final ProxyConnectionPool proxyConnectionPool = ((ProxyConnectionPool)joinPoint.getThis());
        final String uri = proxyConnectionPool.getUri().toString();
        final int numConnectionsPerThread = ((Integer)joinPoint.getArgs()[0]);

        counter.put(threadId, numConnectionsPerThread);
        uris.put(uri, counter);
        int total = 0;
        for (int numConn: uris.get(uri).values()) {
            total += numConn;
        }

        //System.out.println(String.format("%s %d", uri, total));

        return total;
    }

}

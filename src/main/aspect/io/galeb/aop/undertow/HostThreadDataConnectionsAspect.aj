package io.galeb.aop.undertow;

import java.util.Map;

import org.aspectj.lang.JoinPoint;

import io.galeb.core.metrics.CounterConnections;
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
            notify(threadId, thisJoinPoint);
            threadId = "UNDEF";
        }
    }

    private synchronized void notify(final String threadId, final JoinPoint joinPoint) {
        final ProxyConnectionPool proxyConnectionPool = ((ProxyConnectionPool)joinPoint.getThis());
        final String uri = proxyConnectionPool.getUri().toString();
        final int numConnectionsPerThread = ((Integer)joinPoint.getArgs()[0]);

        counter.put(threadId, numConnectionsPerThread);
        uris.put(uri, counter);
        int total = 0;
        for (final int numConn: uris.get(uri).values()) {
            total += numConn;
        }

        CounterConnections.updateMap(uri, total);
    }

}

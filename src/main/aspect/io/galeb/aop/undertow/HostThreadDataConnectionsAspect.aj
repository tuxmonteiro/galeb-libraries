package io.galeb.aop.undertow;

import java.util.Map;

import org.aspectj.lang.JoinPoint;

import io.galeb.core.cdi.WeldContext;
import io.galeb.core.model.Metrics;
import io.galeb.hazelcast.EventBus;
import io.undertow.server.handlers.proxy.ProxyConnectionPool;
import io.undertow.util.CopyOnWriteMap;

public aspect HostThreadDataConnectionsAspect {

    private static final EventBus EVENTBUS = WeldContext.INSTANCE.getBean(EventBus.class);

    private volatile String threadId = "UNDEF";
    private final Map<String, Integer> counter = new CopyOnWriteMap<>();
    private final Map<String, Map<String, Integer>> uris = new CopyOnWriteMap<>();

    pointcut myPointcut() : set(* io.undertow.server.handlers.proxy.ProxyConnectionPool.HostThreadData.connections);

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

        Metrics metrics = new Metrics();
        metrics.setId(uri).getProperties().put(Metrics.PROP_METRICS_TOTAL, total);

        EVENTBUS.sendMetrics(metrics);
    }

}

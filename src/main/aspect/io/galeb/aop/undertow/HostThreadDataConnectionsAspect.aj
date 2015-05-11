/*
 * Copyright (c) 2014-2015 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.aop.undertow;

import io.galeb.core.cdi.WeldContext;
import io.galeb.core.model.Metrics;
import io.galeb.hazelcast.EventBus;
import io.galeb.undertow.util.map.CopyOnWriteMapExpirable;
import io.undertow.server.handlers.proxy.ProxyConnectionPool;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.JoinPoint;

public aspect HostThreadDataConnectionsAspect {

    private static final EventBus EVENTBUS = WeldContext.INSTANCE.getBean(EventBus.class);

    private static final long TTL_THREAD_ID = 1000L; // milliseconds
    private static final long TTL_URI = 1L; // hour

    private volatile String threadId = "UNDEF";
    private final Map<String, Integer> counter = new CopyOnWriteMapExpirable<>(TTL_THREAD_ID, TimeUnit.MILLISECONDS);
    private final Map<String, Map<String, Integer>> uris = new CopyOnWriteMapExpirable<>(TTL_URI, TimeUnit.HOURS);

    pointcut myPointcut() : set(* io.undertow.server.handlers.proxy.ProxyConnectionPool.HostThreadData.connections);

    after() : myPointcut() {
        threadId = thisJoinPoint.getTarget().toString();

        if (thisJoinPoint.getThis() instanceof ProxyConnectionPool) {
            notify(threadId, thisJoinPoint);
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

        final Metrics metrics = new Metrics();
        metrics.setId(uri).putProperty(Metrics.PROP_METRICS_TOTAL, total);

        EVENTBUS.onConnectionsMetrics(metrics);
    }

}

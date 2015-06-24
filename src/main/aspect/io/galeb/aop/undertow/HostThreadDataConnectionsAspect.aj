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
import io.galeb.core.mapreduce.MapReduce;
import io.galeb.hazelcast.mapreduce.BackendConnectionsMapReduce;
import io.galeb.undertow.util.map.CopyOnWriteMapExpirable;
import io.undertow.server.handlers.proxy.ProxyConnectionPool;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public aspect HostThreadDataConnectionsAspect {

    private static final MapReduce MAP_REDUCE = WeldContext.INSTANCE.getBean(BackendConnectionsMapReduce.class);

    private static final long TTL_THREAD_ID = 1L; // hour
    private static final long TTL_URI       = 1L; // hour

    private final Map<String, Integer> counter = new CopyOnWriteMapExpirable<>(TTL_THREAD_ID, TimeUnit.HOURS);
    private final Map<String, Map<String, Integer>> uris = new CopyOnWriteMapExpirable<>(TTL_URI, TimeUnit.HOURS);

    pointcut myPointcut() : set(* io.undertow.server.handlers.proxy.ProxyConnectionPool.HostThreadData.connections);

    after() : myPointcut() {
        if (thisJoinPoint.getThis() instanceof ProxyConnectionPool) {
            synchronized (this) {
                final String threadId = thisJoinPoint.getTarget().toString();
                final ProxyConnectionPool proxyConnectionPool = ((ProxyConnectionPool)thisJoinPoint.getThis());
                final int numConnectionsPerThread = ((Integer)thisJoinPoint.getArgs()[0]);
                notify(threadId, proxyConnectionPool, numConnectionsPerThread);
            }
        }
    }

    public void notify(final String threadId, final ProxyConnectionPool proxyConnectionPool, int numConnectionsPerThread) {
        final String uri = proxyConnectionPool.getUri().toString();
        int total = 0;

        counter.put(threadId, numConnectionsPerThread);
        uris.put(uri, counter);
        for (final int numConn: uris.get(uri).values()) {
            total += numConn;
        }

        MAP_REDUCE.put(uri, total);
    }

}

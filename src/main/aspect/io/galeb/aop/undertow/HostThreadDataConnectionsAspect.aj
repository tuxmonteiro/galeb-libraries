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

import io.galeb.core.util.map.ConnectionMapManager;
import io.undertow.server.handlers.proxy.ProxyConnectionPool;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public aspect HostThreadDataConnectionsAspect {

    private final ConnectionMapManager connectionMapManager = ConnectionMapManager.INSTANCE;

    pointcut myPointcut() : set(* io.undertow.server.handlers.proxy.ProxyConnectionPool.HostThreadData.connections);

    after() : myPointcut() {
        if (thisJoinPoint.getThis() instanceof ProxyConnectionPool) {
            synchronized (this) {
                final String threadId = thisJoinPoint.getTarget().toString();
                final String uri = ((ProxyConnectionPool)thisJoinPoint.getThis()).getUri().toString();
                final int numConnectionsPerThread = ((Integer)thisJoinPoint.getArgs()[0]);
                connectionMapManager.putOnCounterMap(uri, threadId, numConnectionsPerThread);
            }
        }
    }

}

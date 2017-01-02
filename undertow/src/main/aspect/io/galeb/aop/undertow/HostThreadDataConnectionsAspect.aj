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
import io.galeb.fork.undertow.server.handlers.proxy.ProxyConnectionPool;

public aspect HostThreadDataConnectionsAspect {

    private final ConnectionMapManager connectionMapManager = ConnectionMapManager.INSTANCE;

    pointcut myPointcut() : execution(* io.galeb.fork.undertow.server.handlers.proxy.ProxyConnectionPool.handleClosedConnection(*,*)) || execution(* io.undertow.server.handlers.proxy.ProxyConnectionPool.openConnection(*,*,*,*));

    after() : myPointcut() {
        if (thisJoinPoint.getThis() instanceof ProxyConnectionPool) {
            synchronized (this) {
                final ProxyConnectionPool proxyConnectionPool = (ProxyConnectionPool)thisJoinPoint.getThis();
                final String uri = proxyConnectionPool.getUri().toString();
                final int numConnections = proxyConnectionPool.getOpenConnections();
                connectionMapManager.putOnCounterMap(uri, numConnections);
            }
        }
    }

}

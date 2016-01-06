/*
 *  Galeb - Load Balance as a Service Plataform
 *
 *  Copyright (C) 2014-2016 Globo.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.galeb.core.cluster.jcache;

import com.hazelcast.core.*;
import io.galeb.core.cluster.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class NodeLifecycleListener implements LifecycleListener {

    private static Set<ClusterListener> clusterListeners = new CopyOnWriteArraySet<>();
    private static AtomicBoolean ready = new AtomicBoolean(false);

    public static void register(ClusterListener clusterListener) {
        clusterListeners.add(clusterListener);
    }

    public static boolean isReady() {
        return ready.get();
    }

    public static void forceReady() {
        ready.set(true);
    }

    @Override
    public void stateChanged(LifecycleEvent event) {
        switch (event.getState()) {
        case STARTED:
            ready.set(true);
            clusterListeners.forEach(ClusterListener::onClusterReady);
            break;
        case SHUTDOWN:
            ready.set(false);
            break;
        default:
            break;
        }
    }

}

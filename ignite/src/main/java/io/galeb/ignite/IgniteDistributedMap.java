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

package io.galeb.ignite;

import io.galeb.core.cluster.DistributedMap;
import io.galeb.core.cluster.DistributedMapListener;
import io.galeb.core.cluster.DistributedMapStats;
import org.apache.ignite.events.Event;
import org.apache.ignite.lang.IgnitePredicate;

import javax.cache.Cache;
import java.util.*;
import java.util.concurrent.*;

import static io.galeb.ignite.IgniteInstance.INSTANCE;

public class IgniteDistributedMap implements DistributedMap<String, String> {

    private static final Set<DistributedMapListener> LISTENERS = new CopyOnWriteArraySet<>();

    @Override
    public ConcurrentMap<String, String> getMap(String key) {
        final Cache<String, String> cache = INSTANCE.getOrCreateCache(key);
        return null;
    }

    @Override
    public void remove(String key) {
        INSTANCE.destroyCache(key);
    }

    @Override
    public void registerListener(DistributedMapListener distributedMapListener) {
        LISTENERS.add(distributedMapListener);
    }

    @Override
    public void unregisterListener(DistributedMapListener distributedMapListener) {
        LISTENERS.remove(distributedMapListener);
    }

    @Override
    public DistributedMapStats getStats() {
        return new IgniteDistributedMapStats();
    }
}

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
import com.hazelcast.map.listener.*;
import io.galeb.core.cluster.*;
import io.galeb.core.json.*;
import io.galeb.core.model.*;

import javax.cache.*;
import javax.cache.configuration.*;
import javax.enterprise.inject.*;
import java.util.*;
import java.util.concurrent.*;

@Default
public class JCacheDistributedMap implements DistributedMap<String, String> {

    private static final HazelcastInstance HZ = JCacheInstance.getInstance();

    private static final Set<DistributedMapListener> LISTENERS = new CopyOnWriteArraySet<>();

    @Override
    public Cache<String, String> getMap(String key) {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        CompleteConfiguration<String, String> config = new MutableConfiguration<String, String>()
                                                        .setTypes( String.class, String.class );
        Cache<String, String> cache = cacheManager.getCache(key, String.class, String.class);

        CacheEntryListenerConfiguration<String,String> cacheEntryListenerConfiguration = null;
        //cache.registerCacheEntryListener(cacheEntryListenerConfiguration);
        return cache;
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
        return new JCacheDistributedMapStats();
    }

    private static class Listener implements EntryAddedListener<String, String>,
                                      EntryRemovedListener<String, String>,
                                      EntryUpdatedListener<String, String>,
                                      EntryEvictedListener<String, String> {

        private static final Map<String, Class<? extends Entity>> ENTITY_CLASSES = new ConcurrentHashMap<>();
        static {
            ENTITY_CLASSES.put(Backend.class.getSimpleName().toLowerCase(), Backend.class);
            ENTITY_CLASSES.put(BackendPool.class.getSimpleName().toLowerCase(), BackendPool.class);
            ENTITY_CLASSES.put(Rule.class.getSimpleName().toLowerCase(), Rule.class);
            ENTITY_CLASSES.put(VirtualHost.class.getSimpleName().toLowerCase(), VirtualHost.class);
            ENTITY_CLASSES.put(Farm.class.getSimpleName().toLowerCase(), Farm.class);
        }

        private Entity getEntity(EntryEvent<String, String> entry, boolean oldValue) {
            String entryStr = oldValue ? entry.getOldValue() : entry.getValue();
            String entityType = ((Entity) JsonObject.fromJson(entryStr, Entity.class)).getEntityType();
            return (Entity) JsonObject.fromJson(entryStr, ENTITY_CLASSES.get(entityType));
        }

        @Override
        public void entryEvicted(EntryEvent<String, String> entry) {
            final Entity entity = getEntity(entry, true);
            LISTENERS.forEach(distributedMapListener -> distributedMapListener.entryEvicted(entity));
        }

        @Override
        public void entryUpdated(EntryEvent<String, String> entry) {
            final Entity entity = getEntity(entry, false);
            LISTENERS.forEach(distributedMapListener -> distributedMapListener.entryUpdated(entity));
        }

        @Override
        public void entryRemoved(EntryEvent<String, String> entry) {
            final Entity entity = getEntity(entry, true);
            LISTENERS.forEach(distributedMapListener -> distributedMapListener.entryRemoved(entity));
        }

        @Override
        public void entryAdded(EntryEvent<String, String> entry) {
            final Entity entity = getEntity(entry, false);
            LISTENERS.forEach(distributedMapListener -> distributedMapListener.entryAdded(entity));
        }

    }

}

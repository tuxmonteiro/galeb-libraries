package io.galeb.hazelcast;

import io.galeb.core.cluster.DistributedMap;
import io.galeb.core.cluster.DistributedMapListener;
import io.galeb.core.cluster.DistributedMapStats;
import io.galeb.core.json.JsonObject;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.cache.*;
import javax.cache.configuration.*;
import javax.cache.spi.*;
import javax.enterprise.inject.Default;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;

@Default
public class HzDistributedMap implements DistributedMap<String, String> {

    private static final HazelcastInstance HZ = HzInstance.getInstance();

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
        return new HzDistributedMapStats();
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

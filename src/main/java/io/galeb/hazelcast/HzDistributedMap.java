package io.galeb.hazelcast;

import io.galeb.core.cluster.DistributedMap;
import io.galeb.core.cluster.DistributedMapListener;
import io.galeb.core.cluster.DistributedMapStats;
import io.galeb.core.mapreduce.MapReduce;
import io.galeb.core.model.Entity;
import io.galeb.hazelcast.mapreduce.BackendConnectionsMapReduce;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.inject.Default;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

@Default
public class HzDistributedMap implements DistributedMap<String, Entity> {

    private static final HazelcastInstance HZ = HzInstance.getInstance();

    private final Set<DistributedMapListener> distributedMapListeners = new CopyOnWriteArraySet<>();

    @Override
    public ConcurrentMap<String, Entity> getMap(String key) {
        final IMap<String, Entity> map = HZ.getMap(key);
        map.addEntryListener(new Listener(), true);
        return map;
    }

    @Override
    public void registerListener(DistributedMapListener distributedMapListener) {
        distributedMapListeners.add(distributedMapListener);
    }

    @Override
    public void unregisterListener(DistributedMapListener distributedMapListener) {
        distributedMapListeners.remove(distributedMapListener);
    }

    @Override
    public MapReduce getMapReduce() {
        return new BackendConnectionsMapReduce();
    }

    @Override
    public DistributedMapStats getStats() {
        return new HzDistributedMapStats();
    }

    private class Listener implements EntryAddedListener<String, Entity>,
                                      EntryRemovedListener<String, Entity>,
                                      EntryUpdatedListener<String, Entity>,
                                      EntryEvictedListener<String, Entity> {

        @Override
        public void entryEvicted(EntryEvent<String, Entity> entry) {
            final Entity entity = entry.getOldValue().copy();
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryEvicted(entity));
        }

        @Override
        public void entryUpdated(EntryEvent<String, Entity> entry) {
            final Entity entity = entry.getValue().copy();
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryUpdated(entity));
        }

        @Override
        public void entryRemoved(EntryEvent<String, Entity> entry) {
            final Entity entity = entry.getOldValue().copy();
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryRemoved(entity));
        }

        @Override
        public void entryAdded(EntryEvent<String, Entity> entry) {
            final Entity entity = entry.getValue().copy();
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryAdded(entity));
        }

    }

}

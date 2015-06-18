package io.galeb.hazelcast;

import io.galeb.core.cluster.DistributedMap;
import io.galeb.core.cluster.DistributedMapListener;
import io.galeb.core.model.Entity;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.inject.Default;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

@Default
public class HzDistributedMap implements DistributedMap<String, Entity> {

    private final Set<DistributedMapListener> distributedMapListeners = new CopyOnWriteArraySet<>();

    @Override
    public ConcurrentMap<String, Entity> getMap(String key) {
        final ConcurrentMap<String, Entity> map = HzInstance.getInstance().getMap(key);
        ((IMap<String, Entity>) map).addEntryListener(new LocalEntryListener(), true);
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

    private class LocalEntryListener implements EntryAddedListener<String, Entity>,
                                                EntryRemovedListener<String, Entity>,
                                                EntryUpdatedListener<String, Entity>,
                                                EntryEvictedListener<String, Entity>,
                                                MapEvictedListener,
                                                MapClearedListener {

        @Override
        public void mapCleared(MapEvent mapEvent) {
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.mapCleared(mapEvent.getName()));
        }

        @Override
        public void mapEvicted(MapEvent mapEvent) {
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.mapEvicted(mapEvent.getName()));
        }

        @Override
        public void entryEvicted(EntryEvent<String, Entity> entry) {
            final Entry<String, Entity> entryMap = new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryEvicted(entryMap));
        }

        @Override
        public void entryUpdated(EntryEvent<String, Entity> entry) {
            final Entry<String, Entity> entryMap = new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryUpdated(entryMap));
        }

        @Override
        public void entryRemoved(EntryEvent<String, Entity> entry) {
            final Entry<String, Entity> entryMap = new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryRemoved(entryMap));
        }

        @Override
        public void entryAdded(EntryEvent<String, Entity> entry) {
            final Entry<String, Entity> entryMap = new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
            distributedMapListeners.forEach(distributedMapListener -> distributedMapListener.entryAdded(entryMap));
        }

    }

}

package io.galeb.undertow.util.map;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import io.undertow.util.CopyOnWriteMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CopyOnWriteMapExpirable<K, V> implements Map<K, V> {

    private final Map<K, V> realMap = new CopyOnWriteMap<>();
    private final Map<K, Long> timestamps = new CopyOnWriteMap<>();
    private final long ttl;

    public CopyOnWriteMapExpirable(long ttl, TimeUnit timeUnit) {
        this.ttl = timeUnit.toNanos(ttl);
    }

    public CopyOnWriteMapExpirable(long ttlInNanoSec) {
        this(ttlInNanoSec, TimeUnit.NANOSECONDS);
    }

    @Override
    public V get(Object key) {
        final V value = this.realMap.get(key);

        if (value != null && expired(key, value)) {
            realMap.remove(key);
            timestamps.remove(key);
            return null;
        } else {
            return value;
        }
    }

    private boolean expired(Object key, V value) {
        return (System.nanoTime() - timestamps.get(key)) > this.ttl;
    }

    @Override
    public V put(K key, V value) {
        timestamps.put(key, System.nanoTime());
        return realMap.put(key, value);
    }

    @Override
    public int size() {
        return realMap.size();
    }

    @Override
    public boolean isEmpty() {
        return realMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return realMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return realMap.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        timestamps.remove(key);
        return realMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        timestamps.clear();
        realMap.clear();
    }

    @Override
    public Set<K> keySet() {
        clearExpired();
        return unmodifiableSet(realMap.keySet());
    }

    @Override
    public Collection<V> values() {
        clearExpired();
        return unmodifiableCollection(realMap.values());
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        clearExpired();
        return unmodifiableSet(realMap.entrySet());
    }

    public final void clearExpired() {
        for (final K k : realMap.keySet()) {
            this.get(k);
        }
    }

    public final void renewAll() {
        for (final K k : realMap.keySet()) {
            timestamps.put(k, System.nanoTime());
        }
    }

}

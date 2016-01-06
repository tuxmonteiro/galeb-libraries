/*
 * Copyright (c) 2014-2016 Globo.com
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

package io.galeb.ignite;

import com.google.common.collect.*;

import javax.cache.Cache;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

public class ProxyJCacheToConcurrentMap<K, V> implements ConcurrentMap<K, V> {

    private final Cache<K, V> cache;

    public ProxyJCacheToConcurrentMap(final Cache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public synchronized int size() {
        return Math.toIntExact(StreamSupport.stream(cache.spliterator(), false).count());
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        return cache.containsKey((K) key);
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        return StreamSupport.stream(cache.spliterator(), false)
                .filter(entry -> entry.getValue().equals(value)).count() > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        return cache.get((K) key);
    }

    @Override
    public V put(K key, V value) {
        return cache.getAndPut(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        return cache.getAndRemove((K) key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (map != null) {
            cache.putAll(map);
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public synchronized Set<K> keySet() {
        final Set<K> keys = new HashSet<>();
        for (Cache.Entry<K, V> entry : cache) {
            keys.add(entry.getKey());
        }
        return Collections.unmodifiableSet(keys);
    }

    @Override
    public synchronized Collection<V> values() {
        final Collection<V> values = new ArrayList<>();
        for (Cache.Entry<K, V> entry : cache) {
            values.add(entry.getValue());
        }
        return Collections.unmodifiableCollection(values);
    }

    @Override
    public synchronized Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> entries = new HashSet<>();
        cache.forEach(entry -> entries.add(new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return entry.getKey();
            }

            @Override
            public V getValue() {
                return entry.getValue();
            }

            @Override
            public V setValue(Object value) {
                throw new UnsupportedOperationException();
            }
        }));
        return Collections.unmodifiableSet(entries);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return containsKey(key) ? get(key) : defaultValue;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        cache.forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    public V putIfAbsent(K key, V value) {
        V oldValue = cache.get(key);
        cache.putIfAbsent(key, value);
        return oldValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object key, Object value) {
        return cache.remove((K) key);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return cache.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        V oldValue = cache.get(key);
        cache.replace(key, value);
        return oldValue;
    }

}

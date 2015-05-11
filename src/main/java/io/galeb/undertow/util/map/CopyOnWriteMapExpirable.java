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

package io.galeb.undertow.util.map;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import io.undertow.util.CopyOnWriteMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CopyOnWriteMapExpirable<K, V> implements Map<K, V> {

    private class ValueWithTimeStamp {
        private final V value;
        private long timestamp = System.nanoTime();

        public ValueWithTimeStamp(final V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }

        public ValueWithTimeStamp setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public boolean expired(long ttl) {
            return (System.nanoTime() - this.timestamp) > ttl;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            try {
                @SuppressWarnings("unchecked")
                ValueWithTimeStamp valueWithTimeStamp = (ValueWithTimeStamp) obj;
                result = this.value.equals(valueWithTimeStamp.getValue());
            } finally {
                //
            }
            return result;
        }
    }

    private final Map<K, ValueWithTimeStamp> realMap = new CopyOnWriteMap<>();
    private final long ttl;

    public CopyOnWriteMapExpirable(long ttl, TimeUnit timeUnit) {
        this.ttl = timeUnit.toNanos(ttl);
    }

    public CopyOnWriteMapExpirable(long ttlInNanoSec) {
        this(ttlInNanoSec, TimeUnit.NANOSECONDS);
    }

    @Override
    public V get(Object key) {
        final ValueWithTimeStamp valueWithTimeStamp = realMap.get(key);

        if (valueWithTimeStamp != null && valueWithTimeStamp.expired(ttl)) {
            realMap.remove(key);
            return null;
        } else {
            return valueWithTimeStamp.getValue();
        }
    }

    @Override
    public V put(K key, V value) {
        ValueWithTimeStamp valueWithTimeStampStored = realMap.put(key, new ValueWithTimeStamp(value));
        if (valueWithTimeStampStored!=null) {
            return valueWithTimeStampStored.value;
        } else {
            return null;
        }
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
        @SuppressWarnings("unchecked")
        ValueWithTimeStamp valueWithTimeStamp = new ValueWithTimeStamp((V) value);
        return realMap.containsValue(valueWithTimeStamp);
    }

    @Override
    public V remove(Object key) {
        ValueWithTimeStamp valueWithTimeStampStored = realMap.remove(key);
        return valueWithTimeStampStored.getValue();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        realMap.clear();
    }

    @Override
    public Set<K> keySet() {
        clearExpired();
        return unmodifiableSet(realMap.keySet());
    }

    private Map<K, V> extractRealMap() {
        final Map<K, V> mapExtracted = new HashMap<>();
        realMap.forEach((key, valueWithTimeStamp) -> mapExtracted.put(key, valueWithTimeStamp.getValue()));
        return mapExtracted;
    }

    @Override
    public Collection<V> values() {
        clearExpired();
        return unmodifiableCollection(extractRealMap().values());
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        clearExpired();
        return unmodifiableSet(extractRealMap().entrySet());
    }

    public final void clearExpired() {
        realMap.forEach((k, v) -> get(k));
    }

    public final void renewAll() {
        realMap.forEach((k, v) -> v.setTimestamp(System.nanoTime()));
    }

}

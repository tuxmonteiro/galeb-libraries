/*
 * Copyright (c) 2014-2015 Globo.com
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

package io.galeb.infinispan;

import io.galeb.core.cluster.DistributedMap;
import io.galeb.core.cluster.DistributedMapListener;
import io.galeb.core.cluster.DistributedMapStats;
import io.galeb.core.cluster.NullDistributedMapStats;
import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

import javax.enterprise.inject.Default;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Default
public class InfinispanDistributedMap implements DistributedMap<String,String> {

    private static final DefaultCacheManager CACHE_MANAGER = InfinispanInstance.getCacheManager();

    private static final Set<DistributedMapListener> LISTENERS = new CopyOnWriteArraySet<>();

    @Override
    public ConcurrentMap<String, String> getMap(String key) {
        if (CACHE_MANAGER.cacheExists(key)) {
            return CACHE_MANAGER.getCache(key);
        } else {
            Cache<String, String> cache = CACHE_MANAGER.getCache(key, true);
            cache.addListener(new CacheListener());
            return cache;
        }
    }

    @Override
    public void remove(String key) {
        CACHE_MANAGER.removeCache(key);
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
        return new NullDistributedMapStats();
    }

    @Listener(clustered = true, sync = false)
    public static class CacheListener {

        private Entity getEntity(String key, String value) {
            if (value == null) {
                return null;
            }
            String entityType = ((Entity) JsonObject.fromJson(value, Entity.class)).getEntityType();
            return (Entity) JsonObject.fromJson(value, Farm.getClassFromEntityType(entityType));
        }

        @CacheEntryModified
        public void entryUpdated(CacheEntryModifiedEvent<String, String> entry) {
            if (entry.isCommandRetried()) {
                return;
            }
            final Entity entity = getEntity(entry.getKey(), entry.getValue());
            if (entity == null) {
                return;
            }
            LISTENERS.forEach(distributedMapListener -> distributedMapListener.entryUpdated(entity));
        }

        @CacheEntryRemoved
        public void entryRemoved(CacheEntryRemovedEvent<String, String> entry) {
            if (entry.isCommandRetried()) {
                return;
            }
            final Entity entity = getEntity(entry.getKey(), entry.getOldValue());
            if (entity == null) {
                return;
            }
            LISTENERS.forEach(distributedMapListener -> distributedMapListener.entryRemoved(entity));
        }

        @CacheEntryCreated
        public void entryCreated(CacheEntryCreatedEvent<String, String> entry) {
            if (entry.isCommandRetried()) {
                return;
            }
            final Entity entity = getEntity(entry.getKey(), entry.getValue());
            if (entity == null) {
                return;
            }
            LISTENERS.forEach(distributedMapListener -> distributedMapListener.entryAdded(entity));
        }
    }
}

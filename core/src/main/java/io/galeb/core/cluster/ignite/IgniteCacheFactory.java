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

package io.galeb.core.cluster.ignite;

import io.galeb.core.controller.EntityController;
import io.galeb.core.jcache.CacheFactory;
import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.Event;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.cache.Cache;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.galeb.core.model.Entity.SEP_COMPOUND_ID;
import static io.galeb.core.model.Farm.ENTITY_CLASSES;
import static io.galeb.core.model.Farm.getClassNameFromEntityType;
import static io.galeb.core.util.Constants.SysProp.PROP_CLUSTER_CONF;

public class IgniteCacheFactory implements CacheFactory {

    private static final Logger LOGGER = LogManager.getLogger();

    private static IgniteCacheFactory INSTANCE;

    private final List<Integer> EVENTS = new ArrayList<>(Arrays.asList(
            EventType.EVT_NODE_FAILED,
            EventType.EVT_CACHE_NODES_LEFT,
            EventType.EVT_CACHE_REBALANCE_STARTED,
            EventType.EVT_CACHE_REBALANCE_STOPPED,
            EventType.EVT_NODE_JOINED,
            EventType.EVT_NODE_LEFT,
            EventType.EVT_NODE_SEGMENTED,
            EventType.EVT_CACHE_STOPPED));

    private final String configFile = System.getProperty(PROP_CLUSTER_CONF.toString(), "file:///" + System.getenv("PWD") + "/" + PROP_CLUSTER_CONF.def());

    private Ignite ignite = null;
    private Farm farm = null;
    private boolean isStarted = false;

    public synchronized CacheFactory start() {
        if (!isStarted) {
            String logLevel = System.getenv("LOGLEVEL");
            if (logLevel != null && "DEBUG".equals(logLevel)) {
                EVENTS.clear();
                EVENTS.addAll(EventTypeResolver.EVENTS.keySet());
            }
            if (ignite == null) {
                int[] arrayOfEvents = EVENTS.stream().mapToInt(i -> i).toArray();
                String eventsStr = EVENTS.stream().map(EventTypeResolver.EVENTS::get).reduce((t, u) -> t + " " + u).get();

                IgnitePredicate<Event> listener = event -> {
                    final int eventType = event.type();
                    switch (eventType) {
                        case EventType.EVT_CACHE_OBJECT_PUT:
                            putEvent(event);
                            break;
                        case EventType.EVT_CACHE_OBJECT_REMOVED:
                            removedEvent(event);
                            break;
                        case EventType.EVT_NODE_JOINED:
                            nodeJoinedEvent(event);
                            registerEventDetail(event);
                            break;
                        case EventType.EVT_NODE_LEFT:
                            nodeLeftEvent(event);
                            registerEventDetail(event);
                            break;
                        case EventType.EVT_NODE_FAILED:
                            nodeLeftEvent(event);
                        case EventType.EVT_CACHE_NODES_LEFT:
                        case EventType.EVT_CACHE_REBALANCE_STARTED:
                        case EventType.EVT_CACHE_REBALANCE_STOPPED:
                        case EventType.EVT_NODE_SEGMENTED:
                        case EventType.EVT_CACHE_STOPPED:
                            registerEventDetail(event);
                            if (eventType == EventType.EVT_CACHE_STOPPED) {
                                System.exit(1);
                            }
                            break;
                        default:
                            LOGGER.debug(EventTypeResolver.EVENTS.get(event.type()) + ": " + event.toString());
                    }
                    return true;
                };

                ignite = igniteInstance();
                ignite.events().enableLocal(arrayOfEvents);
                ignite.events().localListen(listener, arrayOfEvents);
                LOGGER.info("Enabling tracking events: " + eventsStr);
            }
            isStarted = true;
        }
        return this;
    }

    private void nodeLeftEvent(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventMessage", event.message());
        map.put("eventType", EventTypeResolver.EVENTS.get(event.type()));
        map.put("eventHostnames", event.node().hostNames());
        map.put("currentNodes", getNodesHostname());

        farm.nodeLeftEvent(Collections.unmodifiableMap(map));
    }

    private void nodeJoinedEvent(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventMessage", event.message());
        map.put("eventType", EventTypeResolver.EVENTS.get(event.type()));
        map.put("eventHostnames", event.node().hostNames());
        map.put("currentNodes", getNodesHostname());

        farm.nodeJoinedEvent(Collections.unmodifiableMap(map));
    }

    private void registerEventDetail(Event event) {
        LOGGER.warn(EventTypeResolver.EVENTS.get(event.type()) + ": " + event.toString());
        showActiveNodes();
    }

    private void showActiveNodes() {
        final String[] hostnames = getNodesHostname();
        LOGGER.warn("Active nodes:" + hostnames[0]);
    }

    private String[] getNodesHostname() {
        final String[] hostnames = new String[1];
        ignite.cluster().nodes().forEach(node -> {
            hostnames[0] = (hostnames[0] != null ? hostnames[0] : "") + " " + node.hostNames().stream().reduce((t, u) -> t + " " + u).get();
        });
        return hostnames;
    }

    private Ignite igniteInstance() {
        return Ignition.start(configFile);
    }

    public synchronized CacheFactory listeningPutEvent() {
        EVENTS.add(EventType.EVT_CACHE_OBJECT_PUT);
        return this;
    }

    public synchronized CacheFactory listeningRemoveEvent() {
        EVENTS.add(EventType.EVT_CACHE_OBJECT_REMOVED);
        return this;
    }

    private IgniteCacheFactory() {
        super();
    }

    public static IgniteCacheFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IgniteCacheFactory();
        }
        return INSTANCE;
    }

    @Override
    public Object getClusterInstance() {
        return ignite;
    }

    @SuppressWarnings("unchecked")
    private void removedEvent(Event event) {
        final CacheEvent cacheEvent = (CacheEvent) event;
        String key = cacheEvent.key();
        String cacheName = cacheEvent.cacheName();
        LOGGER.debug("REMOVED:" + cacheName);
        LOGGER.info("Removing :" + key);

        if (farm != null) {
            Class<? extends Entity> clazz;
            try {
                clazz = (Class<? extends Entity>) Class.forName(cacheName);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e);
                return;
            }
            String[] idWithParentId = key.split(SEP_COMPOUND_ID);
            String id = idWithParentId[0];
            final AtomicReference<String> parentId = new AtomicReference<>(null);
            if (idWithParentId.length > 1) {
                parentId.set(idWithParentId[1]);
            }

            final EntityController entityController = farm.getController(clazz.getSimpleName());
            farm.getCollection(clazz).stream()
                    .filter(e -> e.getId().equals(id) &&
                        ((parentId.get() != null && e.getParentId().equals(parentId.get())) ||
                                parentId.get() == null))
                    .forEach(entity ->
            {
                try {
                    entityController.del(entity.copy());
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            });
        } else {
            LOGGER.error("REMOVED event aborted: FARM is NULL");
        }
    }

    private void putEvent(Event event) {
        final CacheEvent cacheEvent = (CacheEvent) event;
        final Entity entity = getEntity(getValue(cacheEvent));
        if (entity == null) {
            LOGGER.error("Entity is NULL. " + cacheEvent.toString());
            return;
        }
        boolean exist = entityContains(entity);
        if (exist) {
            updatedEvent(cacheEvent);
        } else {
            createdEvent(cacheEvent);
        }
    }

    private void updatedEvent(Event event) {
        final CacheEvent cacheEvent = (CacheEvent) event;
        String key = cacheEvent.key();
        String cacheName = cacheEvent.cacheName();
        String value = getValue(cacheEvent);

        if (value == null) {
            LOGGER.warn("Updating " + key + " aborted: VALUE is NULL");
            return;
        }
        String newValue = (String) cacheEvent.newValue();

        LOGGER.debug("UPDATED:" + key);
        LOGGER.debug("UPDATED:" + cacheName);
        if (newValue != null) {
            LOGGER.info("Updating :" + newValue);
        } else {
            LOGGER.warn("Updating : " + value + " (OLD) Fail - has not new value");
            return;
        }

        if (farm != null) {
            final Entity entity = getEntity(newValue);
            if (entity == null) {
                LOGGER.error("Entity is NULL. " + cacheEvent.toString());
                return;
            }
            final EntityController entityController = getController(entity);
            try {
                entityController.change(entity.copy());
            } catch (Exception e) {
                LOGGER.error(e);
            }
        } else {
            LOGGER.error("UPDATED event aborted: FARM is NULL");
        }
    }

    private String getValue(Event event) {
        final CacheEvent cacheEvent = (CacheEvent) event;
        return (String) (cacheEvent.oldValue() != null ?
                            cacheEvent.oldValue() :
                            cacheEvent.newValue());
    }

    private Entity getEntity(String value) {
        String entityType = ((Entity) JsonObject.fromJson(value, Entity.class)).getEntityType();
        if (entityType == null || "entity".equals(entityType) || "".equals(entityType)) {
            LOGGER.error("EntityType is invalid. " + value);
            return null;
        }
        return (Entity) JsonObject.fromJson(value, ENTITY_CLASSES.get(entityType));
    }

    private void createdEvent(Event event) {
        final CacheEvent cacheEvent = (CacheEvent) event;
        String key = cacheEvent.key();
        String cacheName = cacheEvent.cacheName();
        String value = getValue(cacheEvent);

        if (value == null) {
            LOGGER.warn("Creating " + key + " aborted: VALUE is NULL");
            return;
        }

        LOGGER.debug("CREATED:" + key);
        LOGGER.debug("CREATED:" + cacheName);
        LOGGER.info("Creating :" + value);

        if (farm != null) {
            final Entity entity = getEntity(value);
            if (entity == null) {
                LOGGER.error("Entity is NULL. " + cacheEvent.toString());
                return;
            }
            final EntityController entityController = getController(entity);
            try {
                entityController.add(entity.copy());
            } catch (Exception e) {
                LOGGER.error(e);
            }
        } else {
            LOGGER.error("CREATED event aborted: FARM is NULL");
        }
    }

    private EntityController getController(Entity entity) {
        return farm.getController(getClassNameFromEntityType(entity.getEntityType()));
    }

    private boolean entityContains(Entity entity) {
        return farm.contains(entity);
    }

    @Override
    public CacheFactory setFarm(final Farm farm) {
        if (farm != null) {
            this.farm = farm;
        }
        return this;
    }

    @Override
    public Cache<String, String> getCache(String key) {
        return ignite.getOrCreateCache(key);
    }

    private static class EventTypeResolver {

        public final static Map<Integer, String> EVENTS = new HashMap<>();
        static {
            EVENTS.put(1, "EVT_CHECKPOINT_SAVED");
            EVENTS.put(2, "EVT_CHECKPOINT_LOADED");
            EVENTS.put(3, "EVT_CHECKPOINT_REMOVED");
            EVENTS.put(10, "EVT_NODE_JOINED");
            EVENTS.put(11, "EVT_NODE_LEFT");
            EVENTS.put(12, "EVT_NODE_FAILED");
            EVENTS.put(13, "EVT_NODE_METRICS_UPDATED");
            EVENTS.put(14, "EVT_NODE_SEGMENTED");
            EVENTS.put(16, "EVT_CLIENT_NODE_DISCONNECTED");
            EVENTS.put(17, "EVT_CLIENT_NODE_RECONNECTED");
            EVENTS.put(20, "EVT_TASK_STARTED");
            EVENTS.put(21, "EVT_TASK_FINISHED");
            EVENTS.put(22, "EVT_TASK_FAILED");
            EVENTS.put(23, "EVT_TASK_TIMEDOUT");
            EVENTS.put(24, "EVT_TASK_SESSION_ATTR_SET");
            EVENTS.put(25, "EVT_TASK_REDUCED");
            EVENTS.put(30, "EVT_CLASS_DEPLOYED");
            EVENTS.put(31, "EVT_CLASS_UNDEPLOYED");
            EVENTS.put(32, "EVT_CLASS_DEPLOY_FAILED");
            EVENTS.put(33, "EVT_TASK_DEPLOYED");
            EVENTS.put(34, "EVT_TASK_UNDEPLOYED");
            EVENTS.put(35, "EVT_TASK_DEPLOY_FAILED");
            EVENTS.put(40, "EVT_JOB_MAPPED");
            EVENTS.put(41, "EVT_JOB_RESULTED");
            EVENTS.put(43, "EVT_JOB_FAILED_OVER");
            EVENTS.put(44, "EVT_JOB_STARTED");
            EVENTS.put(45, "EVT_JOB_FINISHED");
            EVENTS.put(46, "EVT_JOB_TIMEDOUT");
            EVENTS.put(47, "EVT_JOB_REJECTED");
            EVENTS.put(48, "EVT_JOB_FAILED");
            EVENTS.put(49, "EVT_JOB_QUEUED");
            EVENTS.put(50, "EVT_JOB_CANCELLED");
            EVENTS.put(60, "EVT_CACHE_ENTRY_CREATED");
            EVENTS.put(61, "EVT_CACHE_ENTRY_DESTROYED");
            EVENTS.put(62, "EVT_CACHE_ENTRY_EVICTED");
            EVENTS.put(63, "EVT_CACHE_OBJECT_PUT");
            EVENTS.put(64, "EVT_CACHE_OBJECT_READ");
            EVENTS.put(65, "EVT_CACHE_OBJECT_REMOVED");
            EVENTS.put(66, "EVT_CACHE_OBJECT_LOCKED");
            EVENTS.put(67, "EVT_CACHE_OBJECT_UNLOCKED");
            EVENTS.put(68, "EVT_CACHE_OBJECT_SWAPPED");
            EVENTS.put(69, "EVT_CACHE_OBJECT_UNSWAPPED");
            EVENTS.put(70, "EVT_CACHE_OBJECT_EXPIRED");
            EVENTS.put(71, "EVT_SWAP_SPACE_DATA_READ");
            EVENTS.put(72, "EVT_SWAP_SPACE_DATA_STORED");
            EVENTS.put(73, "EVT_SWAP_SPACE_DATA_REMOVED");
            EVENTS.put(74, "EVT_SWAP_SPACE_CLEARED");
            EVENTS.put(75, "EVT_SWAP_SPACE_DATA_EVICTED");
            EVENTS.put(76, "EVT_CACHE_OBJECT_TO_OFFHEAP");
            EVENTS.put(77, "EVT_CACHE_OBJECT_FROM_OFFHEAP");
            EVENTS.put(80, "EVT_CACHE_REBALANCE_STARTED");
            EVENTS.put(81, "EVT_CACHE_REBALANCE_STOPPED");
            EVENTS.put(82, "EVT_CACHE_REBALANCE_PART_LOADED");
            EVENTS.put(83, "EVT_CACHE_REBALANCE_PART_UNLOADED");
            EVENTS.put(84, "EVT_CACHE_REBALANCE_OBJECT_LOADED");
            EVENTS.put(85, "EVT_CACHE_REBALANCE_OBJECT_UNLOADED");
            EVENTS.put(86, "EVT_CACHE_REBALANCE_PART_DATA_LOST");
            EVENTS.put(96, "EVT_CACHE_QUERY_EXECUTED");
            EVENTS.put(97, "EVT_CACHE_QUERY_OBJECT_READ");
            EVENTS.put(98, "EVT_CACHE_STARTED");
            EVENTS.put(99, "EVT_CACHE_STOPPED");
            EVENTS.put(100, "EVT_CACHE_NODES_LEFT");
            EVENTS.put(116, "EVT_IGFS_FILE_CREATED");
            EVENTS.put(117, "EVT_IGFS_FILE_RENAMED");
            EVENTS.put(118, "EVT_IGFS_FILE_DELETED");
            EVENTS.put(119, "EVT_IGFS_FILE_OPENED_READ");
            EVENTS.put(120, "EVT_IGFS_FILE_OPENED_WRITE");
            EVENTS.put(121, "EVT_IGFS_META_UPDATED");
            EVENTS.put(122, "EVT_IGFS_FILE_CLOSED_WRITE");
            EVENTS.put(123, "EVT_IGFS_FILE_CLOSED_READ");
            EVENTS.put(124, "EVT_IGFS_DIR_CREATED");
            EVENTS.put(125, "EVT_IGFS_DIR_RENAMED");
            EVENTS.put(126, "EVT_IGFS_DIR_DELETED");
            EVENTS.put(127, "EVT_IGFS_FILE_PURGED");
        }
    }
}

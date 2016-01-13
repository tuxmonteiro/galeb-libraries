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

package io.galeb.core.jcache;

import io.galeb.core.controller.EntityController;
import io.galeb.core.json.JsonObject;
import io.galeb.core.logging.Logger;
import io.galeb.core.logging.NullLogger;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgnitePredicate;

import javax.cache.Cache;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.galeb.core.model.Entity.SEP_COMPOUND_ID;
import static io.galeb.core.model.Farm.ENTITY_CLASSES;
import static io.galeb.core.model.Farm.getClassNameFromEntityType;
import static io.galeb.core.util.Constants.SysProp.*;

public class IgniteCacheFactory implements CacheFactory {

    public static final IgniteCacheFactory INSTANCE = new IgniteCacheFactory();

    private final String configFile = System.getProperty(PROP_CLUSTER_CONF.toString(), "file:///" + System.getenv("PWD") + "/" + PROP_CLUSTER_CONF.def());
    private final Ignite ignite;

    private Farm farm = null;
    private Logger logger = new NullLogger();

    private IgniteCacheFactory() {
        super();
        ignite = Ignition.start(configFile);
        ignite.events().withAsync().enableLocal(EventType.EVT_CACHE_OBJECT_PUT,
                                                EventType.EVT_CACHE_OBJECT_REMOVED);
        IgnitePredicate<CacheEvent> listener = evt -> {
            if (evt.type() == EventType.EVT_CACHE_OBJECT_PUT) {
                putEvent(evt);
            }
            if (evt.type() == EventType.EVT_CACHE_OBJECT_REMOVED) {
                removedEvent(evt);
            }
            return true;
        };
        ignite.events().withAsync().localListen(listener,
                           EventType.EVT_CACHE_OBJECT_PUT,
                           EventType.EVT_CACHE_OBJECT_REMOVED);
    }

    @SuppressWarnings("unchecked")
    private void removedEvent(CacheEvent evt) {
        String key = evt.key();
        String cacheName = evt.cacheName();
        logger.debug("REMOVED:" + cacheName);
        logger.info("Removing :" + key);

        if (farm != null) {
            Class<? extends Entity> clazz;
            try {
                clazz = (Class<? extends Entity>) Class.forName(cacheName);
            } catch (ClassNotFoundException e) {
                logger.error(e);
                return;
            }
            String[] idWithParentId = key.split(SEP_COMPOUND_ID);
            String id = idWithParentId[0];
            final AtomicReference<String> parentId = new AtomicReference<>(null);
            if (idWithParentId.length > 1) {
                parentId.set(idWithParentId[1]);
            }

            final EntityController entityController = farm.getController(clazz.getSimpleName());
            List<Entity> entities = farm.getCollection(clazz).stream().filter(e -> e.getId().equals(id) &&
                    ((parentId.get() != null && e.getParentId().equals(parentId.get())) ||
                            parentId.get() == null) ).collect(Collectors.toList());
            entities.forEach(entity -> {
                try {
                    entityController.del(entity.copy());
                } catch (Exception e) {
                    logger.error(e);
                }
            });
        } else {
            logger.error("REMOVED event aborted: FARM is NULL");
        }
    }

    private void putEvent(CacheEvent evt) {
        if (evt.newValue() != null && evt.oldValue() == null) {
            createdEvent(evt);
        } else {
            updatedEvent(evt);
        }
    }

    private void updatedEvent(CacheEvent evt) {
        String key = evt.key();
        String cacheName = evt.cacheName();
        String value = (String) (evt.oldValue() != null ?
                        evt.oldValue() :
                        evt.newValue());

        if (value == null) {
            logger.warn("Updating " + key + " aborted: VALUE is NULL");
            return;
        }

        logger.debug("UPDATED:" + key);
        logger.debug("UPDATED:" + cacheName);
        logger.info("Updating :" + value);

        if (farm != null) {
            String entityType = ((Entity) JsonObject.fromJson(value, Entity.class)).getEntityType();
            Entity entity = (Entity) JsonObject.fromJson(value, ENTITY_CLASSES.get(entityType));
            EntityController entityController = farm.getController(
                    getClassNameFromEntityType(entity.getEntityType()));
            try {
                entityController.change(entity.copy());
            } catch (Exception e) {
                logger.error(e);
            }
        } else {
            logger.error("UPDATED event aborted: FARM is NULL");
        }
    }

    private void createdEvent(CacheEvent evt) {
        String key = evt.key();
        String cacheName = evt.cacheName();
        String value = (String) (evt.oldValue() != null ?
                        evt.oldValue() :
                        evt.newValue());

        if (value == null) {
            logger.warn("Creating " + key + " aborted: VALUE is NULL");
            return;
        }

        logger.debug("CREATED:" + key);
        logger.debug("CREATED:" + cacheName);
        logger.info("Creating :" + value);

        if (farm != null) {
            String entityType = ((Entity) JsonObject.fromJson(value, Entity.class)).getEntityType();
            Entity entity = (Entity) JsonObject.fromJson(value, ENTITY_CLASSES.get(entityType));
            EntityController entityController = farm.getController(
                    getClassNameFromEntityType(entity.getEntityType()));
            try {
                entityController.add(entity.copy());
            } catch (Exception e) {
                logger.error(e);
            }
        } else {
            logger.error("CREATED event aborted: FARM is NULL");
        }
    }

    @Override
    public CacheFactory setFarm(final Farm farm) {
        this.farm = farm;
        return this;
    }

    @Override
    public CacheFactory setLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public Cache<String, String> getCache(String key) {
        return ignite.getOrCreateCache(key);
    }

}

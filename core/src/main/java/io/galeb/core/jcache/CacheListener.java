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

import javax.cache.event.*;

import java.io.Serializable;

import static io.galeb.core.model.Farm.getClassNameFromEntityType;
import static io.galeb.core.model.Farm.ENTITY_CLASSES;

public class CacheListener<K, V> implements CacheEntryCreatedListener<K, V>,
                                            CacheEntryUpdatedListener<K, V>,
                                            CacheEntryRemovedListener<K, V>,
                                            Serializable {

    private Farm farm = null;
    private Logger logger = new NullLogger();




    public CacheListener<K, V> setFarm(final Farm farm) {
        this.farm = farm;
        return this;
    }

    public CacheListener<K, V> setLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable) throws CacheEntryListenerException {
        iterable.forEach(event -> {
            V entryStr = event.getValue();
            logger.info("Creating " + entryStr);
            if (farm != null) {
                String entityType = ((Entity) JsonObject.fromJson((String) entryStr, Entity.class)).getEntityType();
                Entity entity = (Entity) JsonObject.fromJson((String) entryStr, ENTITY_CLASSES.get(entityType));
                EntityController entityController = farm.getController(
                        getClassNameFromEntityType(entity.getEntityType()));
                try {
                    entityController.add(entity.copy());
                } catch (Exception e) {
                    logger.error(e);
                }
            } else {
                logger.error("onCreated event Failed: FARM is NULL");
            }
        });
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable) throws CacheEntryListenerException {
        iterable.forEach(event -> {
            V entryStr = event.getOldValue();
            logger.info("Removing " + entryStr);

            if (farm != null) {
                String entityType = ((Entity) JsonObject.fromJson((String) entryStr, Entity.class)).getEntityType();
                Entity entity = (Entity) JsonObject.fromJson((String) entryStr, ENTITY_CLASSES.get(entityType));
                EntityController entityController = farm.getController(
                        getClassNameFromEntityType(entity.getEntityType()));
                try {
                    entityController.del(entity.copy());
                } catch (Exception e) {
                    logger.error(e);
                }
            } else {
                logger.error("onRemoved event Failed: FARM is NULL");
            }
        });
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable) throws CacheEntryListenerException {
        iterable.forEach(event -> {
            V entryStr = event.getOldValue();
            logger.info("Updating " + entryStr);

            if (farm != null) {
                String entityType = ((Entity) JsonObject.fromJson((String) entryStr, Entity.class)).getEntityType();
                Entity entity = (Entity) JsonObject.fromJson((String) entryStr, ENTITY_CLASSES.get(entityType));
                EntityController entityController = farm.getController(
                        getClassNameFromEntityType(entity.getEntityType()));
                try {
                    entityController.change(entity.copy());
                } catch (Exception e) {
                    logger.error(e);
                }
            } else {
                logger.error("onUpdated event Failed: FARM is NULL");
            }
        });
    }
}

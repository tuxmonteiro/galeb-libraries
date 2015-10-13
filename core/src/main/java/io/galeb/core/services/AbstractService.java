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

package io.galeb.core.services;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import io.galeb.core.cluster.ClusterEvents;
import io.galeb.core.cluster.ClusterListener;
import io.galeb.core.cluster.DistributedMap;
import io.galeb.core.cluster.DistributedMapListener;
import io.galeb.core.controller.EntityController;
import io.galeb.core.json.JsonObject;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.core.statsd.StatsdClient;

import static io.galeb.core.model.Farm.getClassNameFromEntityType;

public abstract class AbstractService implements DistributedMapListener,
                                                 ClusterListener {

    public static final String LOGGER          = "logger";
    public static final String FARM            = "farm";
    public static final String DISTRIBUTEDMAP  = "distributedMap";
    public static final String STATSD          = "statsd";
    public static final String CLUSTER_EVENTS  = "clusterEvents";
    public static final String INTERVAL        = "interval";

    @Inject
    protected Farm farm;

    @Inject
    protected DistributedMap<String, String> distributedMap;

    @Inject
    protected Logger logger;

    @Inject
    protected ClusterEvents clusterEvents;

    @Inject
    protected StatsdClient statsdClient;

    @Inject
    protected ProcessorScheduler processorScheduler;

    private boolean clusterListenerRegistered = false;

    public AbstractService() {
        super();
    }

    private void entityAdd(Entity entity) {
        EntityController entityController = farm.getController(
                getClassNameFromEntityType(entity.getEntityType()));
        try {
            entityController.add(entity.copy());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void registerCluster() {
        clusterEvents.registerListener(this);
        if (clusterEvents.isReady() && !clusterListenerRegistered) {
            onClusterReady();
        }
    }

    protected void prelaunch() {
        registerCluster();
    }

    public Farm getFarm() {
        return farm;
    }

    public Logger getLogger() {
        return logger;
    }

    public DistributedMap<String, String> getDistributedMap() {
        return distributedMap;
    }

    @Override
    public void entryAdded(Entity entity) {
        logger.debug("entryAdded: "+entity.getId()+" ("+entity.getEntityType()+")");
        entityAdd(entity);
    }

    @Override
    public void entryRemoved(Entity entity) {
        logger.debug("entryRemoved: "+entity.getId()+" ("+entity.getEntityType()+")");
        EntityController entityController = farm.getController(
                getClassNameFromEntityType(entity.getEntityType()));
        try {
            entityController.del(entity.copy());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void entryUpdated(Entity entity) {
        logger.debug("entryUpdated: "+entity.getId()+" ("+entity.getEntityType()+")");
        EntityController entityController = farm.getController(
                getClassNameFromEntityType(entity.getEntityType()));
        try {
            entityController.change(entity.copy());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void mapCleared(String mapName) {
        logger.debug("mapCleared: "+mapName);
        EntityController entityController = farm.getController(
                getClassNameFromEntityType(mapName.toLowerCase()));
        try {
            entityController.delAll();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void entryEvicted(Entity entity) {
        logger.debug("entryEvicted: "+entity.getId()+" ("+entity.getEntityType()+")");
        entryRemoved(entity);
    }

    @Override
    public void mapEvicted(String mapName) {
        logger.debug("mapEvicted: "+mapName);
        mapCleared(mapName);
    }

    @Override
    public void onClusterReady() {
        logger.info("== Cluster ready");
        processorScheduler.setupScheduler(logger, farm);
        processorScheduler.startProcessorJob();
        distributedMap.registerListener(this);
        Arrays.asList(Backend.class, BackendPool.class, Rule.class, VirtualHost.class).stream()
            .forEach(clazz -> {
                ConcurrentMap<String, String> map = distributedMap.getMap(clazz.getName());
                map.forEach( (key, value) -> {
                    Entity entity = (Entity) JsonObject.fromJson(value, clazz);
                    entity.setEntityType(clazz.getSimpleName().toLowerCase());
                    entityAdd(entity);
                });
            });
        clusterListenerRegistered = true;
    }

}

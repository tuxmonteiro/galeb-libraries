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

import javax.inject.Inject;

import io.galeb.core.cluster.ClusterLocker;
import io.galeb.core.controller.EntityController;
import io.galeb.core.jcache.CacheFactory;
import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.statsd.StatsdClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.galeb.core.model.Farm.getClassNameFromEntityType;

public abstract class AbstractService {

    private static final Logger LOGGER = LogManager.getLogger(AbstractService.class);

    public static final String LOGGER_KEY      = "logger";
    public static final String FARM_KEY        = "farm";
    public static final String CACHEFACTORY    = "cacheFactory";
    public static final String CLUSTERLOCKER   = "clusterLocker";
    public static final String STATSD          = "statsd";
    public static final String CLUSTER_EVENTS  = "clusterEvents";
    public static final String INTERVAL        = "interval";

    @Inject
    protected Farm farm;

    @Inject
    protected StatsdClient statsdClient;

    @Inject
    protected ProcessorScheduler processorScheduler;

    protected CacheFactory cacheFactory;

    protected ClusterLocker clusterLocker;

    public AbstractService() {
        super();
    }

    protected void entityAdd(String entityStr, Class<?> clazz) {
        Entity entity = (Entity) JsonObject.fromJson(entityStr, clazz);
        entity.setEntityType(clazz.getSimpleName().toLowerCase());
        EntityController entityController = farm.getController(
                getClassNameFromEntityType(entity.getEntityType()));
        try {
            entityController.add(entity.copy());
            LOGGER.warn("Loading entity " + entity + ": " + entityStr );
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    protected void startProcessorScheduler() {
        processorScheduler.setupScheduler(farm);
        processorScheduler.startProcessorJob();
        LOGGER.info("ProcessorScheduler started");
    }

    public Farm getFarm() {
        return farm;
    }

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }

    public ClusterLocker getClusterLocker() {
        return clusterLocker;
    }

}

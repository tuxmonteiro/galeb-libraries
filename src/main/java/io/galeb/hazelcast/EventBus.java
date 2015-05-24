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

package io.galeb.hazelcast;

import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.eventbus.Event;
import io.galeb.core.eventbus.EventBusListener;
import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.json.JsonObject;
import io.galeb.core.logging.Logger;
import io.galeb.core.mapreduce.MapReduce;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Metrics;
import io.galeb.core.model.Metrics.Operation;
import io.galeb.core.queue.QueueManager;
import io.galeb.hazelcast.mapreduce.BackendConnectionsMapReduce;
import io.galeb.hazelcast.queue.HzQueueManager;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

@Default
public class EventBus implements MessageListener<Event>, IEventBus {

    private static final long AGGREGATION_TIME = 1000L;

    private static final String PROP_HAZELCAST_LOGGING_TYPE = "hazelcast.logging.type";

    static {
        if (System.getProperty(PROP_HAZELCAST_LOGGING_TYPE)==null) {
            System.setProperty(PROP_HAZELCAST_LOGGING_TYPE, "log4j2");
        }
    }

    @Inject
    protected Logger logger;

    private static final HazelcastInstance HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance();

    private final Map<String, ITopic<Event>> topics = new HashMap<>();

    private final BackendConnectionsMapReduce mapReduce = new BackendConnectionsMapReduce(HAZELCAST_INSTANCE, logger);

    private EventBusListener eventBusListener = EventBusListener.NULL;

    private QueueManager queueManager;

    private long lastSendTime = System.currentTimeMillis();
    private final Map<String, Metrics> mapOfBanckeds = new ConcurrentHashMap<>(16, 0.9f, 1);

    private ITopic<Event> putAndGetTopic(String topicId) {
        ITopic<Event> topic = topics.get(topicId);
        if (topic==null) {
            topic = HAZELCAST_INSTANCE.getTopic(topicId);
            topic.addMessageListener(this);
            topics.put(topicId, topic);

            logger.debug(String.format("registered at %s", topicId));
        }
        return topic;
    }

    @Override
    public void publishEntity(final Entity entity, final String entityType, final Action action) {

        entity.setEntityType(entityType);
        final Event event = new Event(action, entity);
        final ITopic<Event> topic = putAndGetTopic(action.toString());

        try {
            topic.publish(event);
        } catch (final RuntimeException e) {
            logger.error(e);
        }

    }

    @Override
    public void onRequestMetrics(Metrics metrics) {

        final Metrics metricsInstance = mapOfBanckeds.getOrDefault(metrics.getId(), metrics);
        final Object statusCodeObj = metrics.getProperty(Metrics.PROP_STATUSCODE);
        int statusCode = 200;
        if (statusCodeObj!=null && statusCodeObj instanceof Integer) {
            statusCode = (int) statusCodeObj;
        }
        final String propHttpCode = Metrics.PROP_HTTPCODE_PREFIX + Integer.toString(statusCode);
        metrics.putProperty(propHttpCode, 1);
        metricsInstance.aggregationProperty(metrics,
                                            propHttpCode,
                                            propHttpCode,
                                            Operation.SUM);
        metricsInstance.aggregationProperty(metrics,
                                            Metrics.PROP_REQUESTTIME,
                                            Metrics.PROP_REQUESTTIME_AVG,
                                            Operation.AVG);

        mapOfBanckeds.put(metrics.getId(), metricsInstance);

        if (lastSendTime<System.currentTimeMillis()-AGGREGATION_TIME) {
            for (final Metrics newMetrics: mapOfBanckeds.values()) {
                queueManager.sendEvent(newMetrics);
            }
            lastSendTime = System.currentTimeMillis();
            mapOfBanckeds.clear();
        }

        logger.debug(JsonObject.toJsonString(metrics));
    }

    @Override
    public void onMessage(Message<Event> message) {
        eventBusListener.onEvent(message.getMessageObject());
    }

    @Override
    public IEventBus setEventBusListener(final EventBusListener eventBusListener) {
        this.eventBusListener = eventBusListener;
        return this;
    }

    @Override
    public void start() {
        if (eventBusListener!=EventBusListener.NULL) {
            for (final Action action: EnumSet.allOf(Action.class)) {
                putAndGetTopic(action.toString());
            }
        }
    }

    @Override
    public void stop() {
        HAZELCAST_INSTANCE.shutdown();
    }

    @Override
    public MapReduce getMapReduce() {
        return mapReduce;
    }

    @Override
    public QueueManager getQueueManager() {
        if (queueManager == null) {
            queueManager = new HzQueueManager(HAZELCAST_INSTANCE);
        }
        return queueManager;
    }

    @Override
    public String getClusterId() {
        return HAZELCAST_INSTANCE.getConfig().getGroupConfig().getName();
    }

}

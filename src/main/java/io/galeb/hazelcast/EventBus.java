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

        mapOfBanckeds.put(metrics.getId(), aggregationMetrics(metrics));

        if (lastSendTime<System.currentTimeMillis()-AGGREGATION_TIME) {
            for (final Metrics newMetrics: mapOfBanckeds.values()) {
                queueManager.sendEvent(newMetrics);
            }
            lastSendTime = System.currentTimeMillis();
            mapOfBanckeds.clear();
        }
        logger.debug(JsonObject.toJsonString(metrics));
    }

    private Metrics aggregationMetrics(Metrics metrics) {
        Metrics metricsAggregated = mapOfBanckeds.get(metrics.getId());

        if (metricsAggregated==null) {
            mapOfBanckeds.put(metrics.getId(), metrics);
            metricsAggregated = new Metrics();
            metricsAggregated.setId(metrics.getId());
            metricsAggregated.setParentId(metrics.getParentId());
            metricsAggregated.setProperties(metrics.getProperties());
        }
        final Object statusCodeObj = metrics.getProperty(Metrics.PROP_STATUSCODE);
        int statusCode = 200;
        if (statusCodeObj!=null && statusCodeObj instanceof Integer) {
            statusCode = (int) statusCodeObj;
        }
        final Object statusCodeCountObj = metricsAggregated.getProperty(Metrics.PROP_HTTPCODE_PREFIX + Integer.toString(statusCode));
        int statusCodeCount = 0;
        if (statusCodeCountObj!=null && statusCodeCountObj instanceof Integer) {
            statusCodeCount = (Integer) statusCodeCountObj;
        }
        statusCodeCount += 1;
        metricsAggregated.putProperty(Metrics.PROP_HTTPCODE_PREFIX + Integer.toString(statusCode), statusCodeCount);

        final Object requestTimeObj = metrics.getProperty(Metrics.PROP_REQUESTTIME);
        long requestTime = 0L;
        if (requestTimeObj!=null && requestTimeObj instanceof Long) {
            requestTime = (long) requestTimeObj;
        }
        final Object requestTimeAvgObj = metricsAggregated.getProperty(Metrics.PROP_REQUESTTIME_AVG);
        long requestTimeAvg = requestTime;
        if (requestTimeAvgObj!=null && requestTimeAvgObj instanceof Long) {
            requestTimeAvg = (long) requestTimeAvgObj;
        }
        requestTimeAvg = (requestTime+requestTimeAvg)/2;
        metricsAggregated.putProperty(Metrics.PROP_REQUESTTIME_AVG, requestTimeAvg);

        return metricsAggregated;
    }

    @Override
    public void onConnectionsMetrics(Metrics metrics) {
        mapReduce.addMetrics(metrics);
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

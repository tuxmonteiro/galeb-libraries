package io.galeb.hazelcast;

import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.eventbus.Event;
import io.galeb.core.eventbus.EventBusListener;
import io.galeb.core.eventbus.IEventBus;
import io.galeb.core.json.JsonObject;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Metrics;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

@Default
public class EventBus implements MessageListener<Event>, IEventBus {

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

    private final MapReduce mapReduce = new MapReduce(HAZELCAST_INSTANCE);

    private EventBusListener eventBusListener = EventBusListener.NULL;

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
        // TODO: send request metrics to eventBus
        logger.info(JsonObject.toJsonString(metrics));
    }

    @Override
    public void onConnectionsMetrics(Metrics metrics) {
        mapReduce.addMetrics(metrics);
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

    public MapReduce getMapReduce() {
        return mapReduce;
    }

}

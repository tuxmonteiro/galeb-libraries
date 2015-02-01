package com.openvraas.hazelcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.openvraas.core.controller.EntityController.Action;
import com.openvraas.core.logging.Logger;
import com.openvraas.core.model.Entity;

@Default
public class EventBus implements MessageListener<Event>, IEventBus {

    private static final HazelcastInstance HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance();

    private final Map<String, ITopic<Event>> topics = new HashMap<>();

    private EventBusListener eventBusListener;

    private ITopic<Event> putAndGetTopic(String topicId) {
        ITopic<Event> topic = topics.get(topicId);
        if (topic==null) {
            topic = HAZELCAST_INSTANCE.getTopic( topicId );
            topic.addMessageListener( this );
            eventBusListener.onLog(Logger.Level.DEBUG.toString(), String.format("registered at %s", topicId));
            topics.put(topicId, topic);
        }
        return topic;
    }

    @Override
    public void publishEntity(final Entity entity, final String entityType, final Action action) {

        entity.setEntityType(entityType);

        Event event = new Event(action, entity);

        ITopic<Event> topic = putAndGetTopic(action.toString());

        try {
            topic.publish(event);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(Message<Event> message) {
        onEvent(message.getMessageObject());
    }

    @Override
    public void onEvent(Event event) {
        eventBusListener.onEvent(event);
    }

    @Override
    public IEventBus setEventBusListener(final EventBusListener eventBusListener) {
        this.eventBusListener = eventBusListener;
        return this;
    }

    @Override
    public void start() {
        if (eventBusListener!=null) {
            for (Action action: EnumSet.allOf(Action.class)) {
                putAndGetTopic(action.toString());
            }
        }
    }

}

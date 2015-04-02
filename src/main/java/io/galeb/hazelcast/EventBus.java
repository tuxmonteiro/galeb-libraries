package io.galeb.hazelcast;

import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

@Default
public class EventBus implements MessageListener<Event>, IEventBus {

    private static final HazelcastInstance HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance();

    private final Map<String, ITopic<Event>> topics = new HashMap<>();

    private EventBusListener eventBusListener = EventBusListener.NULL;

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
            for (Action action: EnumSet.allOf(Action.class)) {
                putAndGetTopic(action.toString());
            }
        }
    }

}

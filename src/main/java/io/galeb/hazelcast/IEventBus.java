package io.galeb.hazelcast;

import io.galeb.core.controller.EntityController.Action;
import io.galeb.core.model.Entity;

public interface IEventBus {

    public void publishEntity(Entity entity, String entityType, Action action);

    public IEventBus setEventBusListener(EventBusListener eventBusListener);

    public void start();

}

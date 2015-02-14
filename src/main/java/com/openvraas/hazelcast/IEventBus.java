package com.openvraas.hazelcast;

import com.openvraas.core.controller.EntityController.Action;
import com.openvraas.core.model.Entity;

public interface IEventBus {

    public void publishEntity(Entity entity, String entityType, Action action);

    public IEventBus setEventBusListener(EventBusListener eventBusListener);

    public void start();

}

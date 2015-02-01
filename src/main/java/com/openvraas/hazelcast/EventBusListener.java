package com.openvraas.hazelcast;

public interface EventBusListener {

    public void onEvent(final Event event);

    public void onLog(String levelName, String message);

}

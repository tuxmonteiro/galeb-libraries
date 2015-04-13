package io.galeb.hazelcast;

public interface EventBusListener {

    public static final EventBusListener NULL = new EventBusListener() {
        @Override
        public void onLog(String levelName, String message) {
            return;
        }

        @Override
        public void onEvent(Event event) {
            return;
        }
    };

    public void onEvent(final Event event);

    public void onLog(String levelName, String message);

}

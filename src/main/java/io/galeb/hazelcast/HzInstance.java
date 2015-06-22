package io.galeb.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public final class HzInstance {

    private static final String PROP_HAZELCAST_LOGGING_TYPE = "hazelcast.logging.type";

    static {
        if (System.getProperty(PROP_HAZELCAST_LOGGING_TYPE)==null) {
            System.setProperty(PROP_HAZELCAST_LOGGING_TYPE, "log4j2");
        }
    }

    private static final HazelcastInstance HZ = Hazelcast.newHazelcastInstance();

    private HzInstance() {
        // singleton?
    }

    public static HazelcastInstance getInstance() {
         return HZ;
    }

}

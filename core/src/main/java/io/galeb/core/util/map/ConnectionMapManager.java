package io.galeb.core.util.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConnectionMapManager {

    public static final String PROP_CMM_TTL_URI       = "io.galeb.cmm.ttl.uri";

    static {
        if (System.getProperty(PROP_CMM_TTL_URI)==null) {
            System.setProperty(PROP_CMM_TTL_URI, "3600");
        }
    }

    public static final ConnectionMapManager INSTANCE = new ConnectionMapManager();

    private final ConcurrentHashMapExpirable<String, Integer> uris =
            new ConcurrentHashMapExpirable<>(Long.valueOf(System.getProperty(PROP_CMM_TTL_URI)), TimeUnit.SECONDS, 16, 0.9f, 1);

    private ConnectionMapManager() {
        // SINGLETON
    }

    public void putOnCounterMap(String uri, int count) {
        uris.put(uri, count);
    }

    public void clear() {
        uris.clear();
    }

    public Map<String, Integer> reduce() {
        return Collections.unmodifiableMap(uris);
    }
}

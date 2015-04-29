package io.galeb.hazelcast;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.galeb.core.model.Metrics;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class MapReduce {

    public static final String MAP_ID = UUID.randomUUID().toString();

    private final HazelcastInstance hazelcastInstance;

    private final IMap<String, Integer> mapBackendConn;

    private Long timeOut = 60000L;

    public MapReduce(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        mapBackendConn = hazelcastInstance.getMap(MAP_ID);
    }

    public MapReduce setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public Long getTimeOut() {
        return timeOut;
    }

    public void addMetrics(Metrics metrics) {
        final int metrics_total = (int) metrics.getProperties()
                .get(Metrics.PROP_METRICS_TOTAL);

        mapBackendConn.putAsync(
                metrics.getId(),
                metrics_total,
                timeOut,
                TimeUnit.MILLISECONDS);
    }

    public boolean contains(String backendId) {
        return mapBackendConn.containsKey(backendId);
    }

}

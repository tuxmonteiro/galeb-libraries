package io.galeb.hazelcast.mapreduce;

import io.galeb.core.model.Metrics;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

public class MapReduce {

    public static final String MAP_ID = MapReduce.class.getSimpleName();

    private final HazelcastInstance hazelcastInstance;

    private final IMap<String, Integer> mapBackendConn;

    private Long timeOut = 10000L;

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

    public void addMetrics(final Metrics metrics) {
        final int metricsTotal = (int) metrics.getProperties()
                                              .get(Metrics.PROP_METRICS_TOTAL);

        mapBackendConn.put(metrics.getId(),
                           metricsTotal,
                           timeOut,
                           TimeUnit.MILLISECONDS);
    }

    public boolean contains(String backendId) {
        return mapBackendConn.containsKey(backendId);
    }

    public Map<String, Integer> reduce() throws InterruptedException, ExecutionException {

        final JobTracker jobTracker = hazelcastInstance.getJobTracker("jobTracker1");
        final KeyValueSource<String, Integer> source = KeyValueSource.fromMap(mapBackendConn);

        final Job<String, Integer> job = jobTracker.newJob(source);

        final ICompletableFuture<Map<String, Integer>> future =
                job.mapper(new BackendConnectionsMapper())
                   .combiner(new BackendConnectionsCombiner())
                   .reducer(new BackendConnectionsReducerFactory())
                   .submit();

        return future.get();
    }

}

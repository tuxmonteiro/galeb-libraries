package io.galeb.hazelcast.mapreduce;

import io.galeb.core.logging.Logger;
import io.galeb.core.mapreduce.MapReduce;
import io.galeb.core.model.Metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

public class BackendConnectionsMapReduce implements MapReduce {

    public static final String MAP_ID = BackendConnectionsMapReduce.class.getSimpleName();

    private final HazelcastInstance hazelcastInstance;

    private final Logger logger;

    private final IMap<String, Integer> mapBackendConn;

    private Long timeOut = 10000L;

    public BackendConnectionsMapReduce(final HazelcastInstance hazelcastInstance, final Logger logger) {
        this.hazelcastInstance = hazelcastInstance;
        mapBackendConn = hazelcastInstance.getMap(MAP_ID);
        this.logger = logger;
    }

    @Override
    public MapReduce setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    @Override
    public Long getTimeOut() {
        return timeOut;
    }

    @Override
    public void addMetrics(final Metrics metrics) {
        final int metricsTotal = (int) metrics.getProperties()
                                              .get(Metrics.PROP_METRICS_TOTAL);

        mapBackendConn.put(metrics.getId(),
                           metricsTotal,
                           timeOut,
                           TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean contains(String backendId) {
        return reduce().containsKey(backendId);
    }

    @Override
    public Map<String, Integer> reduce() {

        final JobTracker jobTracker = hazelcastInstance.getJobTracker("jobTracker1");
        final KeyValueSource<String, Integer> source = KeyValueSource.fromMap(mapBackendConn);

        final Job<String, Integer> job = jobTracker.newJob(source);

        final ICompletableFuture<Map<String, Integer>> future =
                job.mapper(new BackendConnectionsMapper())
                   .combiner(new BackendConnectionsCombinerFactory())
                   .reducer(new BackendConnectionsReducerFactory())
                   .submit();

        try {
            return future.get();
        } catch (InterruptedException|ExecutionException e) {
            logger.error(e);
        }
        return new HashMap<>(mapBackendConn);
    }

}

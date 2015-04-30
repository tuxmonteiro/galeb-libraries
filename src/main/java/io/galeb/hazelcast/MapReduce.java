package io.galeb.hazelcast;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.galeb.core.model.Metrics;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import com.hazelcast.mapreduce.Mapper;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

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

        final ICompletableFuture<Map<String, Integer>> future = job.mapper(
                new Mapper<String, Integer, String, Integer>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void map(String key, Integer value, Context<String, Integer> context) {
                        context.emit(key, value);
                    }

                }
                )
                .combiner(new CombinerFactory<String, Integer, Integer>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Combiner<Integer, Integer> newCombiner(String key) {
                        return new Combiner<Integer, Integer>() {

                            private volatile int sum;

                            @Override
                            public void combine(Integer value) {
                                sum += value;
                            }

                            @Override
                            public Integer finalizeChunk() {
                                final int sum = this.sum;
                                this.sum = 0;

                                return sum;
                            }
                        };
                    }
                }
                )
                .reducer(new ReducerFactory<String, Integer, Integer>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Reducer<Integer, Integer> newReducer(String key) {
                        return new Reducer<Integer, Integer>() {

                            private volatile int value = 0;

                            @Override
                            public Integer finalizeReduce() {
                                return value;
                            }

                            @Override
                            public void reduce(Integer value) {
                                this.value += value;
                            }
                        };
                    }
                }
                )
                .submit();

        return future.get();
    }

}

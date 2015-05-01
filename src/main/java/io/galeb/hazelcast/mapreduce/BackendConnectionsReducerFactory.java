package io.galeb.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

final class BackendConnectionsReducerFactory implements
        ReducerFactory<String, Integer, Integer> {
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
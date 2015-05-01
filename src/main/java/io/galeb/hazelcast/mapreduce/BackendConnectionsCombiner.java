package io.galeb.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

final class BackendConnectionsCombiner implements
        CombinerFactory<String, Integer, Integer> {
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
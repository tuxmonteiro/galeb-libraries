package io.galeb.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

final class BackendConnectionsMapper implements
        Mapper<String, Integer, String, Integer> {
    private static final long serialVersionUID = 1L;

    @Override
    public void map(String key, Integer value, Context<String, Integer> context) {
        context.emit(key, value);
    }
}
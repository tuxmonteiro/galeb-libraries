package io.galeb.hazelcast;

import io.galeb.core.cluster.DistributedMapStats;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;

import java.util.Arrays;

import javax.enterprise.inject.Default;

import com.hazelcast.monitor.LocalMapStats;

@Default
public class HzDistributedMapStats implements DistributedMapStats {

    private LocalMapStats localMapStats;

    private String result = "";

    @Override
    public String getStats() {
        result = "";
        Arrays.asList(Backend.class, BackendPool.class, Rule.class, VirtualHost.class)
            .forEach(clazz -> {
                localMapStats = HzInstance.getInstance().getMap( clazz.getName() )
                                                        .getLocalMapStats();
                result += " === ";
                result += clazz.getName();
                result += " === ";
                result += localMapStats.toJson().toString();
                result += " === ";
            });

        result += ".";
        return result;
    }

    @Override
    public String toString() {
        return getStats();
    }

}

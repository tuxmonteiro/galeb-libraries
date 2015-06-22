package io.galeb.hazelcast;

import io.galeb.core.cluster.ClusterEvents;
import io.galeb.core.cluster.ClusterListener;

import javax.enterprise.inject.Default;

@Default
public class HzClusterEvents implements ClusterEvents {

    @Override
    public void registerListener(ClusterListener clusterListener) {
        NodeLifecycleListener.register(clusterListener);
    }

    @Override
    public boolean isReady() {
        return NodeLifecycleListener.isReady();
    }

}

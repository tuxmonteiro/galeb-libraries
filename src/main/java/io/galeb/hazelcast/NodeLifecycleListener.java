package io.galeb.hazelcast;

import io.galeb.core.cluster.ClusterListener;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;

public class NodeLifecycleListener implements LifecycleListener {

    private static Set<ClusterListener> clusterListeners = new CopyOnWriteArraySet<>();
    private static AtomicBoolean ready = new AtomicBoolean(false);

    public static void register(ClusterListener clusterListener) {
        clusterListeners.add(clusterListener);
    }

    public static boolean isReady() {
        return ready.get();
    }

    @Override
    public void stateChanged(LifecycleEvent event) {
        switch (event.getState()) {
        case STARTED:
            ready.set(true);
            clusterListeners.forEach(ClusterListener::onClusterReady);
            break;
        case SHUTDOWN:
            ready.set(false);
            break;
        default:
            break;
        }
    }

}
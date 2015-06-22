package io.galeb.hazelcast;

import io.galeb.core.cluster.ClusterEvents;
import io.galeb.core.cluster.ClusterListener;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.inject.Default;

import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.core.LifecycleListener;

@Default
public class HzClusterEvents implements ClusterEvents {

    private AtomicBoolean ready = new AtomicBoolean(false);

    @Override
    public void registerListener(ClusterListener clusterListener) {
        HzInstance.getInstance().getLifecycleService().addLifecycleListener(new ClusterLifecycleListener(clusterListener));
    }

    @Override
    public boolean isReady() {
        return ready.get();
    }

    private class ClusterLifecycleListener implements LifecycleListener {

        private final ClusterListener clusterListener;

        public ClusterLifecycleListener(ClusterListener clusterListener) {
            this.clusterListener = clusterListener;
        }

        @Override
        public void stateChanged(LifecycleEvent event) {
            System.out.println(event.getState().toString());

            if (event.getState() == LifecycleState.STARTED) {
                ready.set(true);
                clusterListener.onClusterReady();
            }
        }

    }

}

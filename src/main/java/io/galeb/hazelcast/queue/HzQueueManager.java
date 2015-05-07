package io.galeb.hazelcast.queue;

import java.io.Serializable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

import io.galeb.core.queue.QueueListener;
import io.galeb.core.queue.QueueManager;

public class HzQueueManager implements QueueManager, ItemListener<Serializable> {

    private static final String QUEUE_METRICS = "queue_metrics";

    private QueueListener queueListener = QueueListener.NULL;

    private final IQueue<Serializable> queue;

    public HzQueueManager(HazelcastInstance hazelcastInstance) {
        queue = hazelcastInstance.getQueue(QUEUE_METRICS);
        queue.addItemListener(this, true);
    }

    @Override
    public void register(QueueListener queueListener) {
        this.queueListener = queueListener;
    }

    @Override
    public void sendEvent(Serializable data) {
        queue.add(data);
    }

    @Override
    public void itemAdded(ItemEvent<Serializable> item) {
        queueListener.onEvent(item.getItem());
    }

    @Override
    public void itemRemoved(ItemEvent<Serializable> item) {
        // ignore
    }

}

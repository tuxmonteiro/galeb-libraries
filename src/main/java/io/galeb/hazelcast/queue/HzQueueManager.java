/*
 * Copyright (c) 2014-2015 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

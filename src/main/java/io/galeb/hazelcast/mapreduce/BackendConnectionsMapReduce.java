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

package io.galeb.hazelcast.mapreduce;

import io.galeb.core.logging.Logger;
import io.galeb.core.mapreduce.MapReduce;
import io.galeb.hazelcast.HzInstance;
import io.galeb.hazelcast.NodeLifecycleListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

@Default
public class BackendConnectionsMapReduce implements MapReduce {

    public static final String MAP_ID = BackendConnectionsMapReduce.class.getSimpleName();

    private static final HazelcastInstance HZ = HzInstance.getInstance();

    @Inject
    private Logger logger;

    private final IMap<String, Integer> mapBackendConn = HZ.getMap(MAP_ID);

    private Long timeOut = 10000L;

    @Override
    public MapReduce setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    @Override
    public Long getTimeOut() {
        return timeOut;
    }

    @Override
    public void put(String key, int value) {
        mapBackendConn.put(key, value, timeOut, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean contains(String backendId) {
        return reduce().containsKey(backendId);
    }

    @Override
    public Map<String, Integer> reduce() {
        if (!NodeLifecycleListener.isReady()) {
            return Collections.emptyMap();
        }
        final JobTracker jobTracker = HZ.getJobTracker(this.getClass().getName());
        final KeyValueSource<String, Integer> source = KeyValueSource.fromMap(mapBackendConn);

        final Job<String, Integer> job = jobTracker.newJob(source);

        final ICompletableFuture<Map<String, Integer>> future =
                job.mapper(new BackendConnectionsMapper())
                   .combiner(new BackendConnectionsCombinerFactory())
                   .reducer(new BackendConnectionsReducerFactory())
                   .submit();

        try {
            return future.get();
        } catch (InterruptedException|ExecutionException e) {
            if (logger!=null) {
                logger.error(e);
            } else {
                e.printStackTrace();
            }
        }
        return new HashMap<>(mapBackendConn);
    }

}

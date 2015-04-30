package io.galeb.hazelcast;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import io.galeb.core.model.Metrics;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class MapReduceTest {

    private static HazelcastInstance hzInstance;

    private MapReduce mapReduce;
    private Metrics metrics;

    @BeforeClass
    public static void setUpStatic() {
        final Config config = new Config();
        config.getGroupConfig().setName(UUID.randomUUID().toString());
        final NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().setMembers(Arrays.asList(new String[]{"127.0.0.1"}));
        hzInstance = Hazelcast.newHazelcastInstance(config);
    }

    private void initializeMetrics(int numConn) {
        metrics.setId("TEST").getProperties().put(Metrics.PROP_METRICS_TOTAL, numConn);
    }

    @Before
    public void setUp() {
        mapReduce = new MapReduce(hzInstance);
        metrics = new Metrics();
    }

    @Test
    public void addMetricsTest() {
        initializeMetrics(0);

        mapReduce.addMetrics(metrics);

        assertThat(mapReduce.contains(metrics.getId())).isTrue();
    }

    @Test
    public void checkBackendWithTimeout() throws InterruptedException {
        initializeMetrics(0);
        final long timeout = 1L; // MILLISECOND (Hint: 0L = TTL Forever)

        mapReduce.setTimeOut(timeout).addMetrics(metrics);
        Thread.sleep(timeout+1);

        assertThat(mapReduce.contains(metrics.getId())).isFalse();
    }

    @Test
    public void checkLocalMapReduceResult() throws InterruptedException, ExecutionException {
        final int numConn = 10;
        initializeMetrics(numConn);

        mapReduce.addMetrics(metrics);
        final int connections = mapReduce.reduce().get(metrics.getId());

        assertThat(connections).isEqualTo(numConn);
    }

}

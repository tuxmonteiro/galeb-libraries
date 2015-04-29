package io.galeb.hazelcast;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;
import io.galeb.core.model.Metrics;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class MapReduceTest {

    static HazelcastInstance hzInstance;
    //static HazelcastInstance hzInstance2;

    @BeforeClass
    public static void setUp() {
        final Config config = new Config();
        config.getGroupConfig().setName(UUID.randomUUID().toString());
        final NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().setMembers(Arrays.asList(new String[]{"127.0.0.1"}));

        hzInstance = Hazelcast.newHazelcastInstance(config);
        //hzInstance2 = Hazelcast.newHazelcastInstance(config);

    }

    @Test
    public void addMetricsTest() {
        final MapReduce mapReduce = new MapReduce(hzInstance);

        final Metrics metrics = new Metrics();
        metrics.setId("TEST").getProperties().put(Metrics.PROP_METRICS_TOTAL, 0);
        mapReduce.addMetrics(metrics);

        assertThat(mapReduce.contains(metrics.getId())).isTrue();
    }

    @Test
    public void checkBackendWithTimeout() throws InterruptedException {
        final MapReduce mapReduce = new MapReduce(hzInstance);

        final Metrics metrics = new Metrics();
        metrics.setId("TEST").getProperties().put(Metrics.PROP_METRICS_TOTAL, 0);

        final long timeout = 1L; // MILLISECOND (Hint: 0L = TTL Forever)
        mapReduce.setTimeOut(timeout).addMetrics(metrics);
        assertThat(mapReduce.contains(metrics.getId())).isFalse();
    }

//    @Test
//    public void checkMap() {
//        final MapReduce mapReduce1 = new MapReduce(hzInstance);
//        final MapReduce mapReduce2 = new MapReduce(hzInstance2);
//
//        final Metrics metrics1 = new Metrics();
//        metrics1.setId("TEST1").getProperties().put(Metrics.PROP_METRICS_TOTAL, 0);
//        final Metrics metrics2 = new Metrics();
//        metrics2.setId("TEST1").getProperties().put(Metrics.PROP_METRICS_TOTAL, 1);
//
//        mapReduce1.addMetrics(metrics1);
//
//        assertThat(mapReduce1.contains(metrics1.getId())).isTrue();
//        mapReduce2.addMetrics(metrics2);
//
//        assertThat(mapReduce2.contains(metrics1.getId())).isTrue();
//        assertThat(mapReduce1.contains(metrics1.getId())).isTrue();
//    }



}

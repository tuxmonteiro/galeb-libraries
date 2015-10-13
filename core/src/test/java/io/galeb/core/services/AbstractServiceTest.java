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

package io.galeb.core.services;

import static org.assertj.core.api.Assertions.assertThat;
import io.galeb.core.cluster.ClusterEvents;
import io.galeb.core.cluster.DistributedMap;
import io.galeb.core.cluster.DistributedMapListener;
import io.galeb.core.cluster.DistributedMapStats;
import io.galeb.core.controller.BackendController;
import io.galeb.core.controller.BackendPoolController;
import io.galeb.core.controller.EntityController;
import io.galeb.core.controller.RuleController;
import io.galeb.core.controller.VirtualHostController;
import io.galeb.core.logging.impl.Log4j2Logger;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.VirtualHost;
import io.galeb.core.statsd.StatsdClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AbstractServiceTest {

    static class ServiceImplemented extends AbstractService {
        //
    }

    static class FakeFarm extends Farm {
        private static final long serialVersionUID = 1L;
    }

    static class FakeDistributedMapStats implements DistributedMapStats {
        @Override
        public String getStats() {
            return this.getClass().getName();
        }

        @Override
        public String toString() {
            return getStats();
        }
    }

    static class FakeDistributedMap implements DistributedMap<String, String> {
        // Fake

        @Override
        public ConcurrentMap<String, String> getMap(String key) {
            return new ConcurrentHashMap<>();
        }

        @Override
        public void registerListener(DistributedMapListener distributedMapListener) {
            // NULL
        }

        @Override
        public DistributedMapStats getStats() {
            return new FakeDistributedMapStats();
        }
    }

    static class FakeClusterEvents implements ClusterEvents {
        @Override
        public boolean isReady() {
            return true;
        }
    }

    static class FakeStatsdClient implements StatsdClient {
        // Fake
    }

    @Inject
    private AbstractService serviceImplemented;

    private Farm farm;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                         .addClasses(
                                 ServiceImplemented.class,
                                 Log4j2Logger.class,
                                 FakeFarm.class,
                                 FakeDistributedMap.class,
                                 FakeClusterEvents.class,
                                 FakeStatsdClient.class)
                         .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void setUp() {
        farm = serviceImplemented.getFarm();
        farm.clear(BackendPool.class);
        farm.clear(VirtualHost.class);
    }

    @Test
    public void farmInjectedIsNotNull() {
        assertThat(serviceImplemented.getFarm()).isNotNull();
    }

}

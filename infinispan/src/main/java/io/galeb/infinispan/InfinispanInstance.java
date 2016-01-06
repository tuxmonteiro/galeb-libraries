/*
 * Copyright (c) 2014-2015 Globo.com
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

package io.galeb.infinispan;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.io.IOException;

import static io.galeb.core.util.Constants.SysProp.PROP_CLUSTER_CONF;

public class InfinispanInstance {

    private static final EmbeddedCacheManager CACHE_MANAGER;
    static {
        String infinispanCfg = System.getProperty(PROP_CLUSTER_CONF.name(), "infinispan.xml");
        EmbeddedCacheManager CACHE_MANAGER1 = null;
        try {
            CACHE_MANAGER1 = new DefaultCacheManager(infinispanCfg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CACHE_MANAGER = CACHE_MANAGER1;
        }
    }

    private InfinispanInstance() {
        // singleton ?
    }

    public static EmbeddedCacheManager getCacheManager() {
        return CACHE_MANAGER;
    }
}

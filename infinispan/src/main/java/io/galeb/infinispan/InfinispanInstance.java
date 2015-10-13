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

import org.infinispan.configuration.cache.*;
import org.infinispan.configuration.global.*;
import org.infinispan.manager.*;

public class InfinispanInstance {

    private static final DefaultCacheManager CACHE_MANAGER;
    static {
        GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.clustering().cacheMode(CacheMode.DIST_ASYNC);
        CACHE_MANAGER = new DefaultCacheManager(global.build(), builder.build());
    }

    private InfinispanInstance() {
        // singleton ?
    }

    public static DefaultCacheManager getCacheManager() {
        return CACHE_MANAGER;
    }
}

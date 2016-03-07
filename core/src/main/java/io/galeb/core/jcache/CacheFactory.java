/*
 *  Galeb - Load Balance as a Service Plataform
 *
 *  Copyright (C) 2014-2016 Globo.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.galeb.core.jcache;

import io.galeb.core.cluster.ClusterListener;
import io.galeb.core.logging.Logger;
import io.galeb.core.model.Farm;

import javax.cache.Cache;

public interface CacheFactory {

    Object getClusterInstance();

    default CacheFactory setLogger(final Logger logger) {
        return this;
    }

    default CacheFactory setFarm(final Farm farm) {
        return this;
    }

    Cache<String, String> getCache(String key);

    default CacheFactory setListener(ClusterListener clusterListener) { return  this; }

    default CacheFactory start() { return this; }

    default CacheFactory listeningReadEvent() { return this; }

    default CacheFactory listeningPutEvent() { return this; }

    default CacheFactory listeningRemoveEvent() { return this; }

}

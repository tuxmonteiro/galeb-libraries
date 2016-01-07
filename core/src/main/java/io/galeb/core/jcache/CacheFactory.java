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

import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.Arrays;

public class CacheFactory {

    private static final CachingProvider cachingProvider = Caching.getCachingProvider();
    private static final CacheManager cacheManager = cachingProvider.getCacheManager();
    private static final MutableConfiguration<String, String> config =
            new MutableConfiguration<String, String>()
            .setTypes(String.class, String.class)
            .setStatisticsEnabled(false);

    public static void createCache(final CacheListener<String, String> cacheListener) {
        Arrays.asList(Backend.class, BackendPool.class, Rule.class, VirtualHost.class).stream()
                .forEach(clazz -> {
                    CacheEntryListenerConfiguration<String, String> cacheEntryListenerConfiguration =
                            new MutableCacheEntryListenerConfiguration<>(
                                    FactoryBuilder.factoryOf(cacheListener), null, true, false);
                    cacheManager.createCache(clazz.getName(), config).registerCacheEntryListener(cacheEntryListenerConfiguration);
                });
    }

    public static Cache<String, String> getCache(String key) {
        return cacheManager.getCache(key, String.class, String.class);
    }

    private CacheFactory() {
        // singleton?
    }
}

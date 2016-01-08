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

import io.galeb.core.logging.*;
import io.galeb.core.model.*;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.*;
import javax.cache.spi.CachingProvider;
import java.net.*;
import java.util.*;

import static io.galeb.core.util.Constants.SysProp.PROP_CLUSTER_CONF;

public class CacheFactory {

    public static final CacheFactory INSTANCE = new CacheFactory();

    private final String configFile = System.getProperty(PROP_CLUSTER_CONF.name(), "file:///" + System.getenv("PWD") + "/" + PROP_CLUSTER_CONF.def());
    private final CachingProvider cachingProvider = Caching.getCachingProvider();
    private final CacheManager cacheManager = cachingProvider.getCacheManager(URI.create(configFile),null);
    private final CacheListener<String, String> cacheListener = new CacheListener<>();
    private final Set<String> cachesRegistered = new HashSet<>();

    private CacheEntryListenerConfiguration<String, String> cacheEntryListenerConfiguration = null;
    private MutableConfiguration<String, String> cacheConfig = new MutableConfiguration<String, String>()
                                                                    .setTypes(String.class, String.class);
    private Logger logger = new NullLogger();

    private CacheFactory() {
        // singleton?
    }

    public CacheFactory setFarm(final Farm farm) {
        cacheListener.setFarm(farm);
        return this;
    }

    public CacheFactory setLogger(final Logger logger) {
        this.logger = logger;
        cacheListener.setLogger(logger);
        return this;
    }

    public CacheFactory registerCacheConfiguration() {
        cacheConfig = new MutableConfiguration<String, String>()
                .setTypes(String.class, String.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ETERNAL))
                .setStatisticsEnabled(false)
                .addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
        logger.info("Cache Configured");
        return this;
    }

    public CacheFactory registerCacheListenerConfiguration() {
        cacheEntryListenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                FactoryBuilder.factoryOf(cacheListener), null, true, false);
        logger.info("CacheListener Configured");
        return this;
    }

    public void createMaps() {
        Arrays.asList(Backend.class, BackendPool.class, Rule.class, VirtualHost.class).stream()
                .forEach(clazz -> {

                    Iterator<String> cacheNamesIter = cacheManager.getCacheNames().iterator();
                    boolean exist = false;
                    while (cacheNamesIter.hasNext()) {
                        String cacheName = cacheNamesIter.next();
                        if (cacheName != null && cacheName.equals(clazz.getName())) {
                            exist = true;
                            break;
                        }
                    }
                    try {
                        if (!exist) {
                            String cacheName= clazz.getName();
                            cacheManager.createCache(cacheName, cacheConfig);
                            cachesRegistered.add(cacheName);
                            logger.info("Map (JSR107) " + cacheName + " created");
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
        });
    }

    @SuppressWarnings("unchecked")
    public Cache<String, String> getCache(String key) {
        Cache cache = cacheManager.getCache(key);
        try {
            if (!cachesRegistered.contains(key)) {
                cache.registerCacheEntryListener(cacheEntryListenerConfiguration);
            }
        } catch (IllegalArgumentException e) {
            logger.debug(e);
        }
        return cache;
    }

}

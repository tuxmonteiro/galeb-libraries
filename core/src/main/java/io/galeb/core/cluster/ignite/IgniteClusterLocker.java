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

package io.galeb.core.cluster.ignite;

import io.galeb.core.cluster.ClusterLocker;
import io.galeb.core.jcache.CacheFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteSemaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IgniteClusterLocker implements ClusterLocker {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ClusterLocker INSTANCE = new IgniteClusterLocker();

    private Ignite ignite;

    private IgniteClusterLocker() {
        //
    }

    public static ClusterLocker getInstance() {
        return INSTANCE;
    }

    @Override
    public ClusterLocker start() {
        CacheFactory cacheFactory = IgniteCacheFactory.getInstance().start();
        ignite = (Ignite) cacheFactory.getClusterInstance();
        return this;
    }

    @Override
    public String name() {
        StringBuilder name = new StringBuilder();
        try {
            name.append(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            name.append("UNKNOW");
            LOGGER.error(e);
        }
        return name.append("|").append(ignite.cluster().localNode().id()).toString();
    }

    @Override
    public boolean lock(String lockName) {
        IgniteSemaphore semaphore = null;
        boolean result = false;
        try {
            semaphore = ignite.semaphore(lockName, 1, true, true);
            result = semaphore != null && semaphore.tryAcquire();
        } catch (IgniteException e) {
            LOGGER.error(e);
        } finally {
            if (result) {
                LOGGER.info("Locking " + lockName + " applied");
            } else {
                LOGGER.warn("Locking " + lockName + " not possible (" + semaphore + ")");
            }
        }
        return result;
    }

    @Override
    public void release(String lockName) {
        IgniteSemaphore semaphore;
        try {
            semaphore = ignite.semaphore(lockName, 1, true, false);
            if (semaphore != null) {
                semaphore.release();
            }
            if (semaphore != null) {
                semaphore.close();
            }
        } catch (IgniteException e) {
            LOGGER.debug(e);
        } finally {
            LOGGER.info("Lock " + lockName + " released");
        }
    }

}

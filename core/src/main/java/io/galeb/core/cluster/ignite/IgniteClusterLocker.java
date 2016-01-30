package io.galeb.core.cluster.ignite;

import io.galeb.core.cluster.ClusterLocker;
import io.galeb.core.jcache.*;
import io.galeb.core.logging.*;
import org.apache.ignite.*;

public class IgniteClusterLocker implements ClusterLocker {

    public static final ClusterLocker INSTANCE = new IgniteClusterLocker();

    private Logger logger = new NullLogger();
    private Ignite ignite;

    private IgniteClusterLocker() {
        CacheFactory cacheFactory = IgniteCacheFactory.INSTANCE;
        ignite = (Ignite) cacheFactory.getClusterInstance();
    }

    @Override
    public ClusterLocker setLogger(Logger logger) {
        if (logger != null) {
            this.logger = logger;
        }
        return this;
    }

    @Override
    public boolean lock(String lockName) {
        IgniteSemaphore semaphore;
        boolean result = false;
        try {
            semaphore = ignite.semaphore(lockName, 1, true, true);
            result = semaphore != null && semaphore.tryAcquire();
        } catch (IgniteException e) {
            logger.debug(e);
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
        } catch (IgniteException e) {
            logger.debug(e);
        }
    }

    @Override
    public boolean isLocked(String lockName) {
        IgniteSemaphore semaphore;
        boolean result = false;
        try {
            semaphore = ignite.semaphore(lockName, 1, true, false);
            result = semaphore != null;
        } catch (IgniteException e) {
            logger.debug(e);
        }
        return result;
    }

    @Override
    public Object countDownLatch(String name, int count) {
        IgniteCountDownLatch countDownLatch = null;
        try {
            countDownLatch = ignite.countDownLatch(name, count, true, true);
        } catch (Exception e) {
            logger.error(e);
        }
        return countDownLatch;
    }

}

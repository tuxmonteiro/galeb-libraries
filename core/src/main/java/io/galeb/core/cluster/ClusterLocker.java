package io.galeb.core.cluster;

import io.galeb.core.logging.*;

public interface ClusterLocker {

    ClusterLocker setLogger(Logger logger);

    boolean lock(String lockName);

    void release(String lockName);

    boolean isLocked(String lockName);

    Object countDownLatch(String name, int count);

}

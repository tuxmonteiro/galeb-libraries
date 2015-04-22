package io.galeb.undertow.nullable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.xnio.XnioWorker;

public class FakeXnioWorkerTest {

    private final XnioWorker nullXnioWorker = FakeXnioWorker.NULL;

    @Test
    public void shutdownNowTest() {
        assertThat(nullXnioWorker.shutdownNow()).isEmpty();
    }

    @Test
    public void isShutdownTest() {
        assertThat(nullXnioWorker.isShutdown()).isFalse();
    }

    @Test
    public void isTerminatedTest() {
        assertThat(nullXnioWorker.isTerminated()).isFalse();
    }

    @Test
    public void awaitTerminationTest() throws InterruptedException {
        assertThat(nullXnioWorker.awaitTermination(0, null)).isFalse();
    }

    @Test
    public void getIoThreadCountTest() {
        assertThat(nullXnioWorker.getIoThreadCount()).isEqualTo(0);
    }

}

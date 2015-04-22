package io.galeb.undertow.nullable;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.xnio.XnioExecutor.Key;
import org.xnio.XnioIoThread;

public class FakeXnioIoThreadTest {

    private final XnioIoThread nullFakeXnioIoThread = FakeXnioIoThread.NULL;

    @Test
    public void executeAfterTest() {
        assertThat(nullFakeXnioIoThread.executeAfter(null, 0, null)).isInstanceOf(Key.class);
    }

    @Test
    public void executeAtIntervalTest() {
        assertThat(nullFakeXnioIoThread.executeAtInterval(null, 0, null)).isInstanceOf(Key.class);
    }

}

package io.galeb.undertow.nullable;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.xnio.Pooled;

public class FakePolledTest {

    private final Pooled<ByteBuffer> nullPooled = FakePolled.NULL;

    @Test
    public void fakePolledGetResourceTest() {
        assertThat(nullPooled.getResource()).isInstanceOf(ByteBuffer.class);
    }

}

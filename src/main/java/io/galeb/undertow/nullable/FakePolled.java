package io.galeb.undertow.nullable;

import java.nio.ByteBuffer;

import org.xnio.Pooled;

public class FakePolled {

    private FakePolled() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final Pooled<ByteBuffer> NULL = new Pooled<ByteBuffer>() {

        @Override
        public void discard() {
        }

        @Override
        public void free() {
        }

        @Override
        public ByteBuffer getResource()
                throws IllegalStateException {
            return ByteBuffer.allocate(0);
        }

        @Override
        public void close() {
        }
    };
}

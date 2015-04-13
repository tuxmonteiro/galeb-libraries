package io.galeb.undertow.nullable;

import java.nio.ByteBuffer;

import org.xnio.Pooled;

public class FakePolled {

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

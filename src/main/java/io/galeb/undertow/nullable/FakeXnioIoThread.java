package io.galeb.undertow.nullable;

import java.util.concurrent.TimeUnit;

import org.xnio.XnioIoThread;

public class FakeXnioIoThread {

	public static final XnioIoThread NULL = new XnioIoThread(null, 0) {

		@Override
		public void execute(Runnable command) {
		}

		@Override
		public Key executeAfter(Runnable command, long time,
				TimeUnit unit) {
			return new Key() {
				@Override
				public boolean remove() {
					return false;
				}
			};
		}

		@Override
		public Key executeAtInterval(Runnable command,
				long time, TimeUnit unit) {
			return new Key() {
				@Override
				public boolean remove() {
					return false;
				}
			};
		}
	};
}

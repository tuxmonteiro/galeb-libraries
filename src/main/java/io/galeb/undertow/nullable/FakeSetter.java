package io.galeb.undertow.nullable;

import org.xnio.ChannelListener;
import org.xnio.ChannelListener.Setter;
import org.xnio.channels.ConnectedChannel;

public class FakeSetter {

	public static final Setter<ConnectedChannel> NULL = new Setter<ConnectedChannel>() {

		@Override
		public void set(ChannelListener<? super ConnectedChannel> listener) {
		}
	};
}

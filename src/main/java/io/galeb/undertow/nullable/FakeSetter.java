package io.galeb.undertow.nullable;

import org.xnio.ChannelListener;
import org.xnio.ChannelListener.Setter;
import org.xnio.channels.ConnectedChannel;

public class FakeSetter {

    private FakeSetter() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final Setter<ConnectedChannel> NULL = new Setter<ConnectedChannel>() {

        @Override
        public void set(ChannelListener<? super ConnectedChannel> listener) {
            // NULL
        }
    };
}

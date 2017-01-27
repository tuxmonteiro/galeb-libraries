package io.galeb.undertow.fork.websockets.extensions;

import io.galeb.undertow.fork.connector.PooledByteBuffer;
import io.galeb.undertow.fork.websockets.core.WebSocketChannel;

import java.io.IOException;

public class NoopExtensionFunction implements ExtensionFunction {
    public static final ExtensionFunction INSTANCE = new NoopExtensionFunction();

    @Override
    public boolean hasExtensionOpCode() {
        return false;
    }

    @Override
    public int writeRsv(int rsv) {
        return 0;
    }

    @Override
    public PooledByteBuffer transformForWrite(PooledByteBuffer pooledBuffer, WebSocketChannel channel) throws IOException {
        return pooledBuffer;
    }

    @Override
    public PooledByteBuffer transformForRead(PooledByteBuffer pooledBuffer, WebSocketChannel channel, boolean lastFragmentOfFrame) throws IOException {
        return pooledBuffer;
    }

    @Override
    public void dispose() {

    }
}
package io.galeb.undertow.nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import org.xnio.XnioIoThread;
import org.xnio.XnioWorker;
import org.xnio.channels.StreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;
import org.xnio.conduits.WriteReadyHandler;

public class FakeStreamSinkConduit {

    private FakeStreamSinkConduit() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final StreamSinkConduit NULL = new StreamSinkConduit() {

        @Override
        public void terminateWrites() throws IOException {
        }

        @Override
        public boolean isWriteShutdown() {
            return false;
        }

        @Override
        public void resumeWrites() {
        }

        @Override
        public void suspendWrites() {
        }

        @Override
        public void wakeupWrites() {
        }

        @Override
        public boolean isWriteResumed() {
            return false;
        }

        @Override
        public void awaitWritable() throws IOException {
        }

        @Override
        public void awaitWritable(long time, TimeUnit timeUnit) throws IOException {
        }

        @Override
        public XnioIoThread getWriteThread() {
            return FakeXnioIoThread.NULL;
        }

        @Override
        public void setWriteReadyHandler(WriteReadyHandler handler) {
        }

        @Override
        public void truncateWrites() throws IOException {
        }

        @Override
        public boolean flush() throws IOException {
            return false;
        }

        @Override
        public XnioWorker getWorker() {
            return FakeXnioWorker.NULL;
        }

        @Override
        public long transferFrom(FileChannel src, long position, long count)
                throws IOException {
            return 0L;
        }

        @Override
        public long transferFrom(StreamSourceChannel source, long count,
                ByteBuffer throughBuffer) throws IOException {
            return 0L;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return 0;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offs, int len) throws IOException {
            return 0L;
        }

        @Override
        public int writeFinal(ByteBuffer src) throws IOException {
            return 0;
        }

        @Override
        public long writeFinal(ByteBuffer[] srcs, int offset, int length)
                throws IOException {
            return 0L;
        }
    };
}

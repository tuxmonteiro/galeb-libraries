package io.galeb.aop.undertow;

import org.aspectj.lang.JoinPoint;

public aspect IoExceptionOnHttpClientAspect {

    /*
    INTERCEPT BOTH EXCEPTIONS:

2016-12-21 15:03:04,160 DEBUG i.u.client [XNIO-1 I/O-4] Connection closed with IOException java.io.IOException: Connection reset by peer
        at sun.nio.ch.FileDispatcherImpl.read0(Native Method)
        at sun.nio.ch.SocketDispatcher.read(SocketDispatcher.java:39)
        at sun.nio.ch.IOUtil.readIntoNativeBuffer(IOUtil.java:223)
        at sun.nio.ch.IOUtil.read(IOUtil.java:192)
        at sun.nio.ch.SocketChannelImpl.read(SocketChannelImpl.java:380)
        at org.xnio.nio.NioSocketConduit.read(NioSocketConduit.java:286)
        at org.xnio.conduits.PushBackStreamSourceConduit.read(PushBackStreamSourceConduit.java:52)
        at org.xnio.conduits.ConduitStreamSourceChannel.read(ConduitStreamSourceChannel.java:127)
        at io.undertow.client.http.HttpClientConnection$ClientReadListener.handleEvent(HttpClientConnection.java:524)
        at io.undertow.client.http.HttpClientConnection$ClientReadListener.handleEvent(HttpClientConnection.java:487)
        at org.xnio.ChannelListeners.invokeChannelListener(ChannelListeners.java:92)
        at org.xnio.conduits.ReadReadyHandler$ChannelListenerHandler.readReady(ReadReadyHandler.java:66)
        at org.xnio.nio.NioSocketConduit.handleReady(NioSocketConduit.java:88)
        at org.xnio.nio.WorkerThread.run(WorkerThread.java:559)

2016-12-21 15:03:04,160 ERROR i.u.proxy [XNIO-1 I/O-4] UT005028: Proxy request to /healthcheck/ failed java.io.IOException: UT001000: Connection closed
        at io.undertow.client.http.HttpClientConnection$ClientReadListener.handleEvent(HttpClientConnection.java:530)
        at io.undertow.client.http.HttpClientConnection$ClientReadListener.handleEvent(HttpClientConnection.java:487)
        at org.xnio.ChannelListeners.invokeChannelListener(ChannelListeners.java:92)
        at org.xnio.conduits.ReadReadyHandler$ChannelListenerHandler.readReady(ReadReadyHandler.java:66)
        at org.xnio.nio.NioSocketConduit.handleReady(NioSocketConduit.java:88)
        at org.xnio.nio.WorkerThread.run(WorkerThread.java:559)
     */

    pointcut uncaughtExceptionScope() :
            (execution(* io.undertow.client.http.HttpClientConnection.ClientReadListener.handleEvent(..)));

    after() throwing(Throwable t) : uncaughtExceptionScope() && !cflow(adviceexecution())    {
        handleException(thisJoinPoint, t);
    }

    private void handleException(JoinPoint jp, Throwable t)
    {
        if (t.getClass().getName().equals(java.io.IOException.class.getName())) {
        }
    }

    }

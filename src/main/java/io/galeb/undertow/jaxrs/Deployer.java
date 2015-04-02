package io.galeb.undertow.jaxrs;

import java.util.Map;

import javax.ws.rs.core.Application;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

public class Deployer {

    private final Builder builder = Undertow.builder();

    private Application application;

    private String host;

    private int port;

    public Deployer() {
        //
    }

    public Deployer deploy(final Application application) {
        this.application = application;
        return this;
    }

    public Deployer setHost(String host) {
        this.host = host;
        return this;
    }

    public Deployer setPort(int port) {
        this.port = port;
        return this;
    }

    public Deployer setOptions(Map<String, String> options) {

        String ioThreadsStr = options.get("IoThreads");

        if (ioThreadsStr!=null) {
            int ioThreads = Integer.parseInt(ioThreadsStr);
            builder.setIoThreads(ioThreads);
        }
        return this;
    }

    public void start() {
        new UndertowJaxrsServer().deploy(application)
                                 .start(builder.addHttpListener(port, host));
    }

}

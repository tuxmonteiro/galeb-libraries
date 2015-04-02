package io.galeb.undertow.router;

import io.galeb.core.model.Farm;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;

import java.util.HashMap;
import java.util.Map;

public class RouterApplication {

    private int port = 8080;
    private String host = "0.0.0.0";
    private final Map<String, String> options = new HashMap<>();
    private Farm farm = null;

    public RouterApplication setHost(String host) {
        this.host = host;
        return this;
    }

    public RouterApplication setPort(int port) {
        this.port = port;
        return this;
    }

    public RouterApplication setOptions(Map<String, String> options) {
        this.options.putAll(options);
        return this;
    }

    public RouterApplication setFarm(Farm farm) {
        this.farm = farm;
        farm.setOptions(options);
        return this;
    }

    public void start() {
        if (farm==null) {
            return;
        }

        Object rootHandlerObj = farm.getRootHandler();
        HttpHandler rootHandler = null;

        if (rootHandlerObj instanceof HttpHandler) {
            rootHandler = (HttpHandler) rootHandlerObj;
        } else {
            return;
        }
        int iothreads = options.containsKey("IoThreads") ? Integer.parseInt(options.get("IoThreads")) : 4;

        final Undertow router = Undertow.builder().addHttpListener(port, host)
                .setIoThreads(iothreads)
                .setHandler(rootHandler)
                .build();

        router.start();

    }

}

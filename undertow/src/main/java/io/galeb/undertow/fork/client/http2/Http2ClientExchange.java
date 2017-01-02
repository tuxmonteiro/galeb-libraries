/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.galeb.undertow.fork.client.http2;

import java.io.IOException;

import io.galeb.undertow.fork.client.PushCallback;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

import io.galeb.undertow.fork.client.ClientCallback;
import io.galeb.undertow.fork.client.ClientConnection;
import io.galeb.undertow.fork.client.ClientExchange;
import io.galeb.undertow.fork.client.ClientRequest;
import io.galeb.undertow.fork.client.ClientResponse;
import io.galeb.undertow.fork.client.ContinueNotification;
import io.galeb.undertow.fork.protocols.http2.Http2StreamSinkChannel;
import io.galeb.undertow.fork.protocols.http2.Http2StreamSourceChannel;
import io.galeb.undertow.fork.util.AbstractAttachable;
import io.galeb.undertow.fork.util.HeaderMap;

/**
 * @author Stuart Douglas
 */
public class Http2ClientExchange extends AbstractAttachable implements ClientExchange {
    private ClientCallback<ClientExchange> responseListener;
    private ContinueNotification continueNotification;
    private Http2StreamSourceChannel response;
    private ClientResponse clientResponse;
    private ClientResponse continueResponse;
    private final ClientConnection clientConnection;
    private final Http2StreamSinkChannel request;
    private final ClientRequest clientRequest;
    private IOException failedReason;

    private PushCallback pushCallback;

    public Http2ClientExchange(ClientConnection clientConnection, Http2StreamSinkChannel request, ClientRequest clientRequest) {
        this.clientConnection = clientConnection;
        this.request = request;
        this.clientRequest = clientRequest;
    }


    @Override
    public void setResponseListener(ClientCallback<ClientExchange> responseListener) {
        this.responseListener = responseListener;
        if(failedReason != null) {
            responseListener.failed(failedReason);
        }
    }

    @Override
    public void setContinueHandler(ContinueNotification continueHandler) {
        this.continueNotification = continueHandler;
    }

    void setContinueResponse(ClientResponse response) {
        this.continueResponse = response;
        if (continueNotification != null) {
            this.continueNotification.handleContinue(this);
        }
    }

    @Override
    public void setPushHandler(PushCallback pushCallback) {
        this.pushCallback = pushCallback;
    }

    PushCallback getPushCallback() {
        return pushCallback;
    }

    @Override
    public StreamSinkChannel getRequestChannel() {
        return request;
    }

    @Override
    public StreamSourceChannel getResponseChannel() {
        return response;
    }

    @Override
    public ClientRequest getRequest() {
        return clientRequest;
    }

    @Override
    public ClientResponse getResponse() {
        return clientResponse;
    }

    @Override
    public ClientResponse getContinueResponse() {
        return continueResponse;
    }

    @Override
    public ClientConnection getConnection() {
        return clientConnection;
    }

    void failed(final IOException e) {
        failedReason = e;
        if(responseListener != null) {
            responseListener.failed(e);
        }
    }

    void responseReady(Http2StreamSourceChannel result) {
        this.response = result;
        ClientResponse clientResponse = createResponse(result);
        this.clientResponse = clientResponse;
        if (responseListener != null) {
            responseListener.completed(this);
        }
    }

    ClientResponse createResponse(Http2StreamSourceChannel result) {
        HeaderMap headers = result.getHeaders();
        final String status = result.getHeaders().getFirst(Http2ClientConnection.STATUS);
        int statusCode = Integer.parseInt(status);
        headers.remove(Http2ClientConnection.STATUS);
        return new ClientResponse(statusCode, status != null ? status.substring(3) : "", clientRequest.getProtocol(), headers);
    }
}

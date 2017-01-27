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

package io.galeb.undertow.fork.protocols.spdy;

import io.galeb.undertow.fork.server.protocol.framed.AbstractFramedStreamSinkChannel;

/**
 * @author Stuart Douglas
 */
public class SpdyStreamSinkChannel extends AbstractFramedStreamSinkChannel<SpdyChannel, SpdyStreamSourceChannel, SpdyStreamSinkChannel> {

    SpdyStreamSinkChannel(SpdyChannel channel) {
        super(channel);
    }

    @Override
    protected boolean isLastFrame() {
        return false;
    }


}
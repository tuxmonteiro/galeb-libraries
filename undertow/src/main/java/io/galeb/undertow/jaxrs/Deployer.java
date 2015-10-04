/*
 * Copyright (c) 2014-2015 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

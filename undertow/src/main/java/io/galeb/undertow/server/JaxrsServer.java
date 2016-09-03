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

package io.galeb.undertow.server;

import javax.ws.rs.core.Application;

import io.galeb.undertow.server.Server;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

public class JaxrsServer extends Server {

    private final UndertowJaxrsServer undertowJaxrsServer = new UndertowJaxrsServer();
    private final Application application;

    public JaxrsServer(Application application) {
        this.application = application;
    }

    @Override
    public void start() {
        undertowJaxrsServer.deploy(application).start(undertowBuilder.getBuilder());
    }

}

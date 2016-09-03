/*
 * Copyright (c) 2014-2016 Globo.com - ATeam
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

import java.util.Map;

public abstract class Server {

    protected final BuilderFactory undertowBuilder = new BuilderFactory();

    public Server setHost(String host) {
        undertowBuilder.setHost(host);
        return this;
    }

    public Server setPort(int port) {
        undertowBuilder.setPort(port);
        return this;
    }

    public Server setOptions(Map<String, String> options) {
        undertowBuilder.clearOptions();
        undertowBuilder.setOptions(options);
        return this;
    }

    public abstract void start();

}

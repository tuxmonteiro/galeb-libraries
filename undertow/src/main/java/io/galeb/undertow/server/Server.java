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

import io.undertow.Undertow.Builder;
import java.util.Map;

public abstract class Server {

    private final BuilderFactory builderFactory = new BuilderFactory();

    public Server setHost(String host) {
        builderFactory.setHost(host);
        return this;
    }

    public Server setPort(int port) {
        builderFactory.setPort(port);
        return this;
    }

    public Server setOptions(Map<String, String> options) {
        builderFactory.clearOptions();
        builderFactory.setOptions(options);
        return this;
    }

    protected Map<String, String> getOptions() {
        return builderFactory.getOptions();
    }

    protected Builder getBuilder() {
        return builderFactory.getBuilder();
    }

    public abstract void start();

}

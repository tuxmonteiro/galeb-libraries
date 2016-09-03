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

import io.galeb.core.model.Farm;
import io.undertow.server.HttpHandler;

public class RouterApplication extends Server {

    private final Farm farm;

    public RouterApplication(Farm farm) {
        this.farm = farm;
    }

    @Override
    public void start() {
        final Object rootHandlerObj = farm.getRootHandler();
        if (rootHandlerObj instanceof HttpHandler) {
            farm.setOptions(undertowBuilder.getOptions());
            HttpHandler rootHandler = (HttpHandler) rootHandlerObj;
            undertowBuilder.getBuilder().setHandler(rootHandler).build().start();
        }
    }

}

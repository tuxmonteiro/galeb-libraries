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

package io.galeb.core.controller;

import io.galeb.core.json.JsonObject;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Farm;

public class BackendPoolController extends EntityController {

    public BackendPoolController(final Farm farm) {
        super(farm);
    }

    @Override
    public EntityController delAll() throws Exception {
        delAll(BackendPool.class);
        return null;
    }

    @Override
    public String get(String id) {
        return get(BackendPool.class, id);
    }

}

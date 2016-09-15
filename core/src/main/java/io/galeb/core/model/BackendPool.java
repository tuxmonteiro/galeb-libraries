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

package io.galeb.core.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.gson.annotations.Expose;

import io.galeb.core.json.JsonObject;

public class BackendPool extends Entity {

    private static final long serialVersionUID = 1L;

    public static final String CLASS_NAME              = "BackendPool";

    public static final String PROP_HEALTHCHECK_RETURN = "hcBody";

    public static final String PROP_HEALTHCHECK_PATH   = "hcPath";

    public static final String PROP_HEALTHCHECK_HOST   = "hcHost";

    public static final String PROP_HEALTHCHECK_CODE   = "hcStatusCode";

    public static final String PROP_LOADBALANCE_POLICY = "loadBalancePolicy";

    @Expose private final Set<String> backends = new ConcurrentSkipListSet<>();

    public BackendPool() {
        super();
    }

    public BackendPool(BackendPool backendPool) {
        super(backendPool);
        setBackends(backendPool.getBackends());
        updateETag();
    }

    private void setBackends(final Set<String> myBackends) {
        final Set<String> copyBackends = new HashSet<>(myBackends);
        backends.clear();
        backends.addAll(copyBackends);
    }

    private Set<String> getBackends() {
        return backends;
    }

    public BackendPool addBackend(String backendId) {
        backends.add(backendId);
        return this;
    }

    public BackendPool delBackend(String backendId) {
        backends.remove(backendId);
        return this;
    }

    public boolean containBackend(String backendId) {
        return backends.contains(backendId);
    }

    @Override
    public Entity copy() {
        return new BackendPool(this);
    }

}

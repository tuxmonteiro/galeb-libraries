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

import io.galeb.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.gson.annotations.Expose;

public class VirtualHost extends Entity {

    private static final long serialVersionUID = 1L;

    public static final String CLASS_NAME = "VirtualHost";

    @Expose private Set<String> rules = new ConcurrentSkipListSet<>();

    public VirtualHost() {
        super();
    }

    public VirtualHost(VirtualHost virtualhost) {
        super(virtualhost);
        setRules(virtualhost.getRules());
        updateETag();
    }

    private void setRules(Set<String> arules) {
        final Set<String> copyRules = new HashSet<>(arules);
        rules.clear();
        rules.addAll(copyRules);
    }

    public VirtualHost addRule(String ruleId) {
        rules.add(ruleId);
        return this;
    }

    public VirtualHost delRule(String ruleId) {
        rules.remove(ruleId);
        return this;
    }

    public boolean containRule(String ruleId) {
        return rules.contains(ruleId);
    }

    public Set<String> getRules() {
        return rules;
    }

    @Override
    public Entity copy() {
        return new VirtualHost(this);
    }

}

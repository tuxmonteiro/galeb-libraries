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

import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;

import java.util.Map;

public class RuleController extends EntityController {

    public RuleController(final Farm farm) {
        super(farm);
    }

    @Override
    public EntityController delAll() throws Exception {
        delAll(Rule.class);
        return this;
    }

    @Override
    public String get(String id) {
        return get(Rule.class, id);
    }


    @Override
    public EntityController add(Entity entity) throws Exception {
        fixRuleOrder(entity);
        return super.add(entity);
    }

    @Override
    public EntityController change(Entity entity) throws Exception {
        fixRuleOrder(entity);
        return super.change(entity);
    }

    private void fixRuleOrder(final Entity entity) {
        if (entity instanceof Rule) {
            Rule rule = (Rule)entity;
            Map<String, Object> properties = rule.getProperties();
            if (properties.get(Rule.PROP_RULE_ORDER) == null) {
                properties.put(Rule.PROP_RULE_ORDER, Integer.MAX_VALUE);
                rule.setProperties(properties);
            }
        }
    }
}

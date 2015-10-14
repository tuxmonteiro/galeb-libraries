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

public class Rule extends Entity {

    private static final long serialVersionUID = 1L;

    public static final String CLASS_NAME       = "Rule";

    public static final String PROP_TARGET_ID   = "targetId";

    public static final String PROP_MATCH       = "match";

    public static final String PROP_RULE_TYPE   = "ruleType";

    public static final String PROP_TARGET_TYPE = "targetType";

    public static final String PROP_RULE_ORDER  = "order";

    public static final String PROP_DEFAULT     = "default";

    public Rule() {
        super();
    }

    public Rule(Rule rule) {
        super(rule);
        updateETag();
    }

    @Override
    public Entity copy() {
        return new Rule(this);
    }

    @Override
    public int compareTo(Entity entity) {
        if (entity == null || !(entity instanceof Rule)) {
            return -1;
        }

        Rule that = (Rule) entity;

        if (that.equals(this)) {
            return 0;
        }
        return this.getRuleOrder() - that.getRuleOrder();
    }

    public int getRuleOrder() {
        final String ruleOrderStr = (String) getProperty(Rule.PROP_RULE_ORDER);
        return ruleOrderStr == null ? Integer.MAX_VALUE : Integer.valueOf(ruleOrderStr);
    }

    public String getMatch() {
        return (String)getProperty(Rule.PROP_MATCH);
    }

    public String getRuleType() {
        return (String)getProperty(Rule.PROP_RULE_TYPE);
    }

    public String getTargetType() {
        return (String)getProperty(Rule.PROP_TARGET_TYPE);
    }

    public String getTargetId() {
        return (String)getProperty(Rule.PROP_TARGET_ID);
    }

    public boolean isDefault() {
        return Boolean.getBoolean((String)getProperty(Rule.PROP_DEFAULT));
    }
}

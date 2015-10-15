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
            return super.compareTo(entity);
        }
        Rule that = (Rule) entity;
        int diff = this.getRuleOrder() - that.getRuleOrder();
        return diff == 0 ? super.compareTo(entity) : diff;
    }

    @Override
    public boolean equals(Object entity) {
        return super.equals(entity);
    }

    public int getRuleOrder() {
        Object ruleOrderObj = getProperty(Rule.PROP_RULE_ORDER);
        if (ruleOrderObj instanceof Integer) {
            return (Integer)ruleOrderObj;
        }
        if (ruleOrderObj instanceof Float) {
            return Math.round((Float)ruleOrderObj);
        }
        if (ruleOrderObj instanceof Double) {
            return ((Double)ruleOrderObj).intValue();
        }
        if (ruleOrderObj instanceof String) {
            final String ruleOrderStr = (String) ruleOrderObj;
            try {
                return Integer.valueOf(ruleOrderStr);
            } catch (NumberFormatException ignore) {
                return Integer.MAX_VALUE;
            }
        }
        return Integer.MAX_VALUE;
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
        Object defaultObj = getProperty(Rule.PROP_DEFAULT);
        if (defaultObj instanceof Boolean) {
            return (Boolean)defaultObj;
        }
        if (defaultObj instanceof Integer) {
            return (Integer)defaultObj == 1;
        }
        if (defaultObj instanceof String) {
            String defaultStr = (String) getProperty(Rule.PROP_DEFAULT);
            return Boolean.getBoolean(defaultStr);
        }
        return false;
    }
}

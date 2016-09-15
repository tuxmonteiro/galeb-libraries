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

package io.galeb.core.util;

import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Constants {

    public static final List<Class<? extends Entity>> ENTITY_CLASSES = Collections.unmodifiableList(
            Arrays.asList(Backend.class, BackendPool.class, Rule.class, VirtualHost.class));

    public static final Map<String, Class<? extends Entity>> ENTITY_MAP = Collections.unmodifiableMap(
            ENTITY_CLASSES.stream().collect(Collectors.toMap(clazz -> clazz.getSimpleName().toLowerCase(),
                                                             clazz -> clazz)));

    public enum SysProp {
        PROP_ENABLE_ACCESSLOG  ("io.galeb.accesslog"        , Boolean.toString(false)),
        PROP_MAXCONN           ("io.galeb.maxConn"          , String.valueOf(1000)),
        PROP_SCHEDULER_INTERVAL("io.galeb.schedulerInterval", String.valueOf(1000)),
        PROP_HOSTNAME          ("io.galeb.fqdn"             , System.getenv("HOSTNAME")),
        PROP_REUSE_XFORWARDED  ("io.galeb.reuseXForwarded"  , Boolean.toString(false)),
        PROP_CLUSTER_CONF      ("io.galeb.cluster.conf"     , "cluster.xml");

        private final String name;
        private final String defaultStr;

        SysProp(String name, String defaultStr) {
            this.name = name;
            this.defaultStr = defaultStr;
        }

        @Override
        public String toString() {
            return name;
        }

        public String def() {
            return defaultStr;
        }
    }

    private Constants() {
        // static dictionary only
    }

}

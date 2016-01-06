/*
 *  Galeb - Load Balance as a Service Plataform
 *
 *  Copyright (C) 2014-2016 Globo.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.galeb.core.cluster.jcache;

import com.hazelcast.monitor.*;
import io.galeb.core.cluster.*;
import io.galeb.core.model.*;

import javax.enterprise.inject.*;
import java.util.*;

@Default
public class JCacheDistributedMapStats implements DistributedMapStats {

    private LocalMapStats localMapStats;

    private String result = "";

    @Override
    public String getStats() {
        result = "";
        Arrays.asList(Backend.class, BackendPool.class, Rule.class, VirtualHost.class)
            .forEach(clazz -> {
                localMapStats = JCacheInstance.getInstance().getMap( clazz.getName() )
                                                        .getLocalMapStats();
                result += " === ";
                result += clazz.getName();
                result += " === ";
                result += localMapStats.toJson().toString();
                result += " === ";
            });

        result += ".";
        return result;
    }

    @Override
    public String toString() {
        return getStats();
    }

}

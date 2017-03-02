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

package io.galeb.core.loadbalance.impl;

import io.galeb.core.loadbalance.LoadBalancePolicy;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class LeastConnPolicy extends LoadBalancePolicy {

    public static final String PROP_CUTTING_LINE = "lbCuttingLine";

    private ConcurrentLinkedQueue<Integer> connectionsOrdered = new ConcurrentLinkedQueue<>();
    private double cuttingLine = 0.666;

    @SuppressWarnings("unchecked")
    @Override
    public int getChoice() {
        Object criteriaConnections = loadBalancePolicyCriteria.get(CRITERIA_CONNECTIONS_COUNTER);
        if (criteriaConnections instanceof LinkedHashMap && connectionsOrdered.isEmpty()) {
            LinkedHashMap<Integer, Integer> connections;
            try {
                connections = (LinkedHashMap<Integer, Integer>) criteriaConnections;
                connectionsOrdered.addAll(connections.entrySet()
                        .stream().sorted(Comparator.comparingInt(Map.Entry::getValue))
                        .limit(Integer.toUnsignedLong((int) ((connections.size()*cuttingLine) - Float.MIN_VALUE)))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(LinkedList::new)));
            } catch (ClassCastException ex) {
                return 0;
            }
        }
        return connectionsOrdered.isEmpty() ? 0 : connectionsOrdered.poll();
    }

    @Override
    public LoadBalancePolicy setCriteria(Map<String, Object> criteria) {
        super.setCriteria(criteria);
        Double limitObj = (Double) loadBalancePolicyCriteria.get(PROP_CUTTING_LINE);
        if (limitObj!=null) {
            cuttingLine = limitObj;
        }
        return this;
    }

}

/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

import java.util.*;

import io.galeb.core.loadbalance.LoadBalancePolicy;
import io.galeb.core.util.consistenthash.ConsistentHash;
import io.galeb.core.util.consistenthash.HashAlgorithm;
import io.galeb.core.util.consistenthash.HashAlgorithm.HashType;

public class HashPolicy extends LoadBalancePolicy {

    public static final String DEFAULT_KEY = "NULL";

    public static final String HASH_ALGORITHM = "HashAlgorithm";

    public static final String NUM_REPLICAS   = "NumReplicas";

    private final Set<Integer> listPos = new TreeSet<>();

    private HashAlgorithm hashAlgorithm = new HashAlgorithm(HashType.SIP24);

    private int numReplicas = 1;

    private final ConsistentHash<Integer> consistentHash =
            new ConsistentHash<>(hashAlgorithm, numReplicas, Collections.emptyList());

    private void reloadPos() {
        listPos.clear();
        final LinkedList<String> linkedList = new LinkedList<>(uris);
        for (final String uri: linkedList) {
            listPos.add(linkedList.indexOf(uri));
        }
    }

    @Override
    public int getChoice() {
        if (isReseted()) {
            reloadPos();
            consistentHash.rebuild(hashAlgorithm, numReplicas, listPos);
            rebuilt();
        }
        return consistentHash.get(aKey.orElse(DEFAULT_KEY));
    }

    @Override
    public synchronized LoadBalancePolicy setCriteria(final Map<String, Object> criteria) {
        super.setCriteria(criteria);
        final String hashAlgorithmStr = (String) criteria.get(HASH_ALGORITHM);
        if (hashAlgorithmStr!=null && HashAlgorithm.hashTypeFromString(hashAlgorithmStr)!=null) {
            hashAlgorithm = new HashAlgorithm(hashAlgorithmStr);
        }
        final String numReplicaStr = (String) criteria.get(NUM_REPLICAS);
        if (numReplicaStr!=null) {
            numReplicas = Integer.valueOf(numReplicaStr);
        }
        reloadPos();
        return this;
    }

}

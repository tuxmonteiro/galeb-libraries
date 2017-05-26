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

package io.galeb.core.util.consistenthash;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class ConsistentHash<T> {

    /** The {@link HashAlgorithm Hash Algorithm} */
    private HashAlgorithm               hashAlgorithm;

    /** The number of replicas. */
    private int                         numberOfReplicas;

    /** The circle. */
    private final SortedMap<Integer, T> circle  = new TreeMap<>();

    /**
     * Instantiates a new consistent hash.
     *
     * @param hashAlgorithm The {@link HashAlgorithm Hash Algorithm}
     * @param numberOfReplicas the number of replicas
     * @param nodes nodes collection
     */
    public ConsistentHash(HashAlgorithm hashAlgorithm, int numberOfReplicas,
            Collection<T> nodes) {
        this.hashAlgorithm = hashAlgorithm;
        this.numberOfReplicas = numberOfReplicas;
        nodes.forEach(this::add);
    }

    /**
     * Adds a node.
     *
     * @param node a node
     */
    public void add(T node) {
        IntStream.range(0, numberOfReplicas).forEach(i ->
            circle.put(hashAlgorithm.hash(node.toString() + i).asInt(), node));
    }

    /**
     * Removes a node.
     *
     * @param node a node
     */
    public void remove(T node) {
        IntStream.range(0, numberOfReplicas).forEach(i ->
            circle.remove(hashAlgorithm.hash(node.toString() + i).asInt()));
    }

    /**
     * Gets a node.
     *
     * @param key a key
     * @return a node from cicle
     */
    public T get(String key) {
        if (circle.isEmpty()) {
            return null;
        }

        int hash= hashAlgorithm.hash(key).asInt();

        if (!circle.containsKey(hash)) {
            SortedMap<Integer, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

    /**
     * Rebuild the cicle.
     *
     * @param hashAlgorithm The {@link HashAlgorithm Hash Algorithm}
     * @param numberOfReplicas the number of replicas
     * @param nodes nodes collection
     */
    public void rebuild(HashAlgorithm hashAlgorithm, Integer numberOfReplicas,
            Collection<T> nodes) {
        if (hashAlgorithm!=null) {
            this.hashAlgorithm = hashAlgorithm;
        }
        if (numberOfReplicas!=null) {
            this.numberOfReplicas = numberOfReplicas;
        }

        circle.clear();
        nodes.forEach(this::add);
    }

}

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

package io.galeb.hazelcast.mapreduce;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

final class BackendConnectionsCombinerFactory implements
        CombinerFactory<String, Integer, Integer> {
    private static final long serialVersionUID = 1L;

    @Override
    public Combiner<Integer, Integer> newCombiner(String key) {
        return new Combiner<Integer, Integer>() {

            private volatile int sum;

            @Override
            public void combine(Integer value) {
                sum += value;
            }

            @Override
            public Integer finalizeChunk() {
                final int result = this.sum;
                this.sum = 0;

                return result;
            }
        };
    }
}
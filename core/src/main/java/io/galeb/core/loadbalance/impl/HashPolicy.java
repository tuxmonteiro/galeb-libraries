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

import com.google.common.hash.Hashing;
import io.galeb.core.loadbalance.LoadBalancePolicy;

import java.nio.charset.Charset;

import static com.google.common.hash.Hashing.sipHash24;

public class HashPolicy extends LoadBalancePolicy {

    public static final String DEFAULT_KEY = "NULL";

    @Override
    public int getChoice() {
        return Hashing.consistentHash(sipHash24().hashString(aKey.orElse(DEFAULT_KEY), Charset.defaultCharset()), uris.size());
    }

}

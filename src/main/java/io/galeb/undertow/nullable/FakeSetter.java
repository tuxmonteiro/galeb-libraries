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

package io.galeb.undertow.nullable;

import org.xnio.ChannelListener;
import org.xnio.ChannelListener.Setter;
import org.xnio.channels.ConnectedChannel;

public class FakeSetter {

    private FakeSetter() {
        // Utility classes, which are a collection of static members,
        // are not meant to be instantiated.
    }

    public static final Setter<ConnectedChannel> NULL = new Setter<ConnectedChannel>() {

        @Override
        public void set(ChannelListener<? super ConnectedChannel> listener) {
            // NULL
        }
    };
}

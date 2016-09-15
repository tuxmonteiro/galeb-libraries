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

package io.galeb.core.model.collections;

import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Entity;

import java.util.List;
import java.util.Set;

public interface Collection<T extends Entity, R extends Entity> extends Set<Entity> {

    List<Entity> getListByID(String entityId);

    List<Entity> getListByJson(JsonObject json);

    Collection<T, R> change(Entity entity);

    default Collection<T, R> defineSetOfRelatives(Collection<? extends Entity, ? extends Entity> relatives) { return this; }

}

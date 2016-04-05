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

package io.galeb.core.cluster;

import io.galeb.core.model.Entity;

public interface DistributedMapListener {

    default void mapCleared(String mapName) {
        throw new UnsupportedOperationException();
    }

    default void mapEvicted(String mapName) {
        throw new UnsupportedOperationException();
    }

    default void entryEvicted(Entity entity) {
        throw new UnsupportedOperationException();
    }

    default void entryUpdated(Entity entity) {
        throw new UnsupportedOperationException();
    }

    default void entryRemoved(Entity entity) {
        throw new UnsupportedOperationException();
    }

    default void entryAdded(Entity entity) {
        throw new UnsupportedOperationException();
    }

    default void showStatistic() {
        //
    }

}

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

package io.galeb.core.loadbalance.hash;

public interface KeyTypeLocator {

    public enum KeyType {
        SOURCE_IP("sourceIP"),
        COOKIE("cookie"),
        URI("uri");

        private final String simpleName;
        private KeyType(String simpleName) {
            this.simpleName = simpleName;
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

    static final KeyTypeLocator NULL = new KeyTypeLocator() {
        // NULL
    };

    static final String DEFAULT_KEY_TYPE = KeyType.SOURCE_IP.toString();

    default ExtractableKey getKey(String keyType) {
        return ExtractableKey.NULL;
    }

}

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

package io.galeb.hazelcast.serializer;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;
import com.hazelcast.nio.*;
import com.hazelcast.nio.serialization.*;
import io.galeb.core.json.*;

import java.io.*;
import java.util.zip.*;

public class EntitySerializer implements StreamSerializer<JsonObject> {

    private final boolean compress;

    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(JsonObject.class);
            return kryo;
        }
    };

    public EntitySerializer(boolean compress) {
        this.compress = compress;
    }

    @Override
    public void write(ObjectDataOutput objectDataOutput, JsonObject entity) throws IOException {
        Kryo kryo = kryoThreadLocal.get();

        if (compress) {
            ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream(16384);
            DeflaterOutputStream deflaterOutputStream =
                    new DeflaterOutputStream(byteArrayOutputStream);
            Output output = new Output(deflaterOutputStream);
            kryo.writeObject(output, entity);
            output.close();

            byte[] bytes = byteArrayOutputStream.toByteArray();
            objectDataOutput.write(bytes);
        } else {
            Output output = new Output((OutputStream) objectDataOutput);
            kryo.writeObject(output, entity.toString());
            output.flush();
        }
    }

    @Override
    public JsonObject read(ObjectDataInput objectDataInput) throws IOException {
        InputStream in = (InputStream) objectDataInput;
        if (compress) {
            in = new InflaterInputStream(in);
        }
        Input input = new Input(in);
        Kryo kryo = kryoThreadLocal.get();
        return kryo.readObject(input, JsonObject.class);
    }

    @Override
    public int getTypeId() {
        return 2;
    }

    @Override
    public void destroy() {
        // empty
    }
}

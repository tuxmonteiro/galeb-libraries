package com.openvraas.hazelcast;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.openvraas.core.json.JsonObject;
import com.openvraas.core.model.Entity;

public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    @Expose
    private final Object type;

    @Expose
    private final Entity data;

    public Event(Object type, Entity data) {
        this.type = type;
        this.data = data;
    }

    public Object getType() {
        return type;
    }

    public JsonObject getData() {
        return new JsonObject(JsonObject.toJson(data));
    }

}

package com.naragas.myweb;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;

public class Category {
    private String id;
    private String name;

    public Category(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        return obj;
    }

    public static Category fromJsonObject(JSONObject obj) throws JSONException {
        return new Category(obj.getString("id"), obj.getString("name"));
    }

    @Override
    public String toString() {
        return name;
    }
}

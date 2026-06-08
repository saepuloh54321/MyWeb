package com.naragas.myweb;

import org.json.JSONException;
import org.json.JSONObject;

public class WebSite {
    private String name;
    private String url;

    public WebSite(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("url", url);
        return obj;
    }

    public static WebSite fromJsonObject(JSONObject obj) throws JSONException {
        return new WebSite(obj.getString("name"), obj.getString("url"));
    }
}

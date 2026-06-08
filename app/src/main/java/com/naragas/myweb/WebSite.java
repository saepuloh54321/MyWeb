package com.naragas.myweb;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WebSite {
    private String name;
    private String url;
    private long lastAccessed;
    private long createdAt;
    private long updatedAt;

    public WebSite(String name, String url) {
        this.name = name;
        this.url = url;
        this.lastAccessed = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public WebSite(String name, String url, long lastAccessed, long createdAt, long updatedAt) {
        this.name = name;
        this.url = url;
        this.lastAccessed = lastAccessed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getFormattedLastAccessed() {
        if (lastAccessed == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return " (" + sdf.format(new Date(lastAccessed)) + ")";
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("url", url);
        obj.put("lastAccessed", lastAccessed);
        obj.put("createdAt", createdAt);
        obj.put("updatedAt", updatedAt);
        return obj;
    }

    public static WebSite fromJsonObject(JSONObject obj) throws JSONException {
        long now = System.currentTimeMillis();
        return new WebSite(
            obj.getString("name"), 
            obj.getString("url"), 
            obj.optLong("lastAccessed", 0),
            obj.optLong("createdAt", now),
            obj.optLong("updatedAt", now)
        );
    }
}

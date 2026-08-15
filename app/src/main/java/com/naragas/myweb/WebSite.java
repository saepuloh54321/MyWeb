package com.naragas.myweb;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WebSite {
    private String name;
    private String url;
    private String description;
    private String categoryId;
    private long lastAccessed;
    private long createdAt;
    private long updatedAt;

    public WebSite(String name, String url) {
        this.name = name;
        this.url = url;
        this.description = "";
        this.categoryId = "";
        this.lastAccessed = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public WebSite(String name, String url, String description, String categoryId, long lastAccessed, long createdAt, long updatedAt) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.categoryId = categoryId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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
        obj.put("description", description);
        obj.put("categoryId", categoryId);
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
            obj.optString("description", ""),
            obj.optString("categoryId", ""),
            obj.optLong("lastAccessed", 0),
            obj.optLong("createdAt", now),
            obj.optLong("updatedAt", now)
        );
    }
}

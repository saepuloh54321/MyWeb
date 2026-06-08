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

    public WebSite(String name, String url) {
        this.name = name;
        this.url = url;
        this.lastAccessed = 0;
    }

    public WebSite(String name, String url, long lastAccessed) {
        this.name = name;
        this.url = url;
        this.lastAccessed = lastAccessed;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public String getFormattedLastAccessed() {
        if (lastAccessed == 0) return "";
        // Format: Tanggal Bulan Tahun, Jam:Menit (contoh: 15 Jan 2024, 10:30)
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return " (" + sdf.format(new Date(lastAccessed)) + ")";
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("url", url);
        obj.put("lastAccessed", lastAccessed);
        return obj;
    }

    public static WebSite fromJsonObject(JSONObject obj) throws JSONException {
        return new WebSite(
            obj.getString("name"), 
            obj.getString("url"), 
            obj.optLong("lastAccessed", 0)
        );
    }
}

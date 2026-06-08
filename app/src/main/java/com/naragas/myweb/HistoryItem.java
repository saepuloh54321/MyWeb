package com.naragas.myweb;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryItem {
    private String url;
    private long timestamp;

    public HistoryItem(String url, long timestamp) {
        this.url = url;
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("url", url);
        obj.put("timestamp", timestamp);
        return obj;
    }

    public static HistoryItem fromJsonObject(JSONObject obj) throws JSONException {
        return new HistoryItem(obj.getString("url"), obj.getLong("timestamp"));
    }
}

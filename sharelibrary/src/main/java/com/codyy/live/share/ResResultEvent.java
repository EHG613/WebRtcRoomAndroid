package com.codyy.live.share;

import org.json.JSONObject;

public class ResResultEvent {
    private JSONObject object;

    public ResResultEvent(JSONObject object) {
        this.object = object;
    }

    public JSONObject getObject() {
        return object;
    }

    public void setObject(JSONObject object) {
        this.object = object;
    }
}

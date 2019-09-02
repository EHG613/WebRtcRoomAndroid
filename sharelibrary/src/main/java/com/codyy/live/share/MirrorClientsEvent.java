package com.codyy.live.share;

import java.util.ArrayList;
import java.util.List;

public class MirrorClientsEvent {
  private List<String> mirrors;

    public MirrorClientsEvent(List<String> mirrors) {
        this.mirrors = mirrors;
    }

    public List<String> getMirrors() {
        return mirrors==null?new ArrayList<>():mirrors;
    }

    public void setMirrors(List<String> mirrors) {
        this.mirrors = mirrors;
    }
}

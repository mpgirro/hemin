package io.hemin.engine.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum FeedStatus {

    NEVER_CHECKED("never_checked"),
    DOWNLOAD_SUCCESS("download_success"),
    HTTP_403("http_403"),
    DOWNLOAD_ERROR("download_error"),
    PARSE_ERROR("parse_error");

    private String name;

    private static final Map<String,FeedStatus> STATUS_MAP;

    static {
        final Map<String,FeedStatus> map = new ConcurrentHashMap<>();
        for (FeedStatus instance : FeedStatus.values()) {
            map.put(instance.getName(),instance);
        }
        STATUS_MAP = Collections.unmodifiableMap(map);
    }

    private FeedStatus(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public static FeedStatus getByName (String name) {
        return STATUS_MAP.get(name);
    }

}

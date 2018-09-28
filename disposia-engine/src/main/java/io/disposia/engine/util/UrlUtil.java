package io.disposia.engine.util;

public class UrlUtil {

    public static String sanitize(String url){
        if (url == null) {
            return null;
        }

        return url
            .replace("<","")
            .replace(">","")
            .replace("\n", "")
            .replace("\t", "")
            .replace("\r", "");
    }

}

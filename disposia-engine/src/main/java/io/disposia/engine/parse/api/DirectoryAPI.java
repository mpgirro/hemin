package io.disposia.engine.parse.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Maximilian Irro
 */
public abstract class DirectoryAPI {

    protected Gson gson = new Gson();

    public abstract String getURL();

    public abstract List<String> getFeedUrls(int count) throws IOException;

    protected String get(String url) throws IOException {
        return new Scanner(new URL(url).openStream(), "UTF-8")
            .useDelimiter("\\A")
            .next();
    }

    protected Map<String, Object> jsonToMap(String json){
        final Type type = new TypeToken<Map<String, Object>>(){}.getType();
        final Map<String, Object> result = gson.fromJson(json, type);
        return result;
    }

    protected List<Map<String, Object>> jsonToListMap(String json){
        final Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
        final List<Map<String, Object>> result = gson.fromJson(json, type);
        return result;
    }

}

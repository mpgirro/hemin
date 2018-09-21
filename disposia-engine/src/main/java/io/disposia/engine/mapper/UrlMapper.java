package io.disposia.engine.mapper;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Maximilian Irro
 */
public class UrlMapper {

    public static UrlMapper INSTANCE = new UrlMapper();

    public String asString(URL url) {
        return url != null ? url.toExternalForm() : null;
    }

    public URL asURL(String url) {
        try {
            return url != null ? new URL(url) : null;
        } catch (MalformedURLException e) {
            throw new RuntimeException( e );
        }
    }
}

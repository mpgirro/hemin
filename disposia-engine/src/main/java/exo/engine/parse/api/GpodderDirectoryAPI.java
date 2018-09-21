package exo.engine.parse.api;

import java.io.IOException;
import java.util.List;

/**
 * Minimalistic(!) wrapper around the gpodder.net DirectoryAPI
 *
 *
 * For DirectoryAPI documentation, see https://gpoddernet.readthedocs.io/en/latest/api/
 *
 * @author Maximilian Irro
 */
public class GpodderDirectoryAPI extends DirectoryAPI {

    private static String API_URL = "https://gpodder.net/api";

    @Override
    public String getURL() {
        return API_URL;
    }

    @Override
    public List<String> getFeedUrls(int count) {
        throw new UnsupportedOperationException("GpodderDirectoryAPI.getFeedUrls(count)");
    }

    public String getPodcasts(int count) throws IOException {
        return get("https://gpodder.net/toplist/"+count+".json");
    }

}

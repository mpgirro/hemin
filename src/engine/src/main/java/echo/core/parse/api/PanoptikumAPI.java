package echo.core.parse.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Minimalistic(!) wrapper around the panoptikum.io API
 *
 *
 * For API documentation, see https://blog.panoptikum.io/api/
 *
 * @author Maximilian Irro
 */
public class PanoptikumAPI extends API {

    private static final Logger log = LoggerFactory.getLogger(PanoptikumAPI.class);

    private static String API_URL = "https://panoptikum.io/jsonapi";

    @Override
    public String getURL() {
        return API_URL;
    }

    @Override
    public List<String> getFeedUrls(int count) {
        throw new UnsupportedOperationException("PanoptikumAPI.getFeedUrls(count)");
    }

    public String getPodcasts(int size) throws IOException {
        if(size > 1000){
            log.error("PanoptikumAPI.getPodcasts(count) argument musst be less than 1000 due to Panoptikum API limitations");
        }
        return get(API_URL+"/podcasts?size="+size);
    }

}

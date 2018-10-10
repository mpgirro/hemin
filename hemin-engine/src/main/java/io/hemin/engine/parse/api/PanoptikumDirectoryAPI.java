package io.hemin.engine.parse.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Minimalistic(!) wrapper around the panoptikum.io DirectoryAPI
 *
 *
 * For DirectoryAPI documentation, see https://blog.panoptikum.io/api/
 *
 */
public class PanoptikumDirectoryAPI extends DirectoryAPI {

    private static final Logger log = LoggerFactory.getLogger(PanoptikumDirectoryAPI.class);

    private static String API_URL = "https://panoptikum.io/jsonapi";

    @Override
    public String getURL() {
        return API_URL;
    }

    @Override
    public List<String> getFeedUrls(int count) {
        throw new UnsupportedOperationException("PanoptikumDirectoryAPI.getFeedUrls(count)");
    }

    public String getPodcasts(int size) throws IOException {
        if(size > 1000){
            log.error("PanoptikumDirectoryAPI.getPodcasts(count) argument musst be less than 1000 due to Panoptikum DirectoryAPI limitations");
        }
        return get(API_URL+"/podcasts?size="+size);
    }

}

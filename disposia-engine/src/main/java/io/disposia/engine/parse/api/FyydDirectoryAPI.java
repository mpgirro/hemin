package io.disposia.engine.parse.api;

import io.disposia.engine.olddomain.ImmutableOldEpisode;
import io.disposia.engine.olddomain.OldEpisode;
import io.disposia.engine.mapper.DateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Minimalistic(!) wrapper around the fyyd.de DirectoryAPI
 *
 *
 * For DirectoryAPI documentation, see https://github.com/eazyliving/fyyd-api
 */
public class FyydDirectoryAPI extends DirectoryAPI {

    private static final Logger log = LoggerFactory.getLogger(FyydDirectoryAPI.class);

    private static String API_URL = "https://api.fyyd.de/0.2";

    @Override
    public String getURL() {
        return API_URL;
    }

    @Override
    public List<String> getFeedUrls(int count) throws IOException {
        final Map<String,Object> apiData = jsonToMap(getPodcasts(count));
        if(apiData.containsKey("data")){
            //final List<Map<String,Object>> data = jsonToListMap((String) apiData.get("data"));
            final List<Map<String,Object>> data = (List<Map<String,Object>>) apiData.get("data");
            if(data.size() > 0){
                return data.stream()
                    .filter(d -> d.containsKey("xmlURL"))   // sanity check if field is present
                    .map(d -> d.get("xmlURL"))              // extract only feed url
                    .map(d -> (String) d)                   // by type it is still an object, so make it a string
                    .collect(Collectors.toList());
            }
        }

        return new LinkedList<>();
    }

    public String getPodcasts(int count) throws IOException {
        return get(API_URL+"/podcasts?count="+count);
    }

    public String getPodcastJSON(String id) throws IOException {
        return get(API_URL+"/podcast/?podcast_id="+id); // TODO schaut die DirectoryAPI wirklich so aus?
    }

    public String getEpisodesByPodcastIdJSON(Long id) throws IOException {
        return getEpisodesByPodcastIdJSON(id, 500);
    }

    public String getEpisodesByPodcastIdJSON(Long id, int count) throws IOException {
        final String endpoint = API_URL+"/podcast/episodes?podcast_id="+id+"&count="+count;
        log.info("GET " + endpoint);
        return get(endpoint);
    }

    public String getEpisodesByPodcastSlugJSON(String slug) throws IOException {
        return getEpisodesByPodcastSlugJSON(slug, 500);
    }

    public String getEpisodesByPodcastSlugJSON(String slug, int count) throws IOException {
        final String endpoint = API_URL+"/podcast/episodes?podcast_slug="+slug+"&count="+count;
        log.info("GET " + endpoint);
        return get(endpoint);
    }

    public List<OldEpisode> getEpisodes(String json) {
        final Map<String,Object> apiData = jsonToMap(json);
        if (apiData.containsKey("data")) {
            final Map<String,Object> data = (Map<String,Object>) apiData.get("data");
            if (data.containsKey("episodes")) {
                final List<Map<String,Object>> episodesObj = (List<Map<String,Object>>) data.get("episodes");
                if (episodesObj != null) {
                    log.info("JSON contains {} episode JSON objects", episodesObj.size());
                    final List<OldEpisode> episodes =  episodesObj.stream()
                        .map(d -> {
                            final ImmutableOldEpisode.Builder e = ImmutableOldEpisode.builder();
                            e.setTitle((String) d.get("title"));
                            e.setLink((String) d.get("url"));
                            e.setDescription((String) d.get("description"));
                            try {
                                // Fyyd produces ZonedDateTime timestamps, therefore I do String -> ZonedDateTime -> LocalDateTime
                                e.setPubDate(DateMapper.INSTANCE.asLocalDateTime(DateMapper.INSTANCE.asZonedDateTime((String) d.get("pubdate"))));
                            } catch (RuntimeException ex) {
                                log.warn("Error parsing pubDate : '{}' [reason : {}]", (String) d.get("pubdate"), ex.getMessage());
                            }
                            // e.setItunesDuration(TODO); // duration is in int format, we need to convert, or save as int as well?
                            e.setGuid((String) d.get("guid"));
                            e.setItunesSeason(((Double) d.get("num_season")).intValue());
                            e.setItunesEpisode(((Double) d.get("num_episode")).intValue());
                            e.setEnclosureUrl((String) d.get("enclosure"));
                            e.setEnclosureType((String) d.get("content_type"));
                            return e.create();
                        })
                        .collect(Collectors.toList());

                    // fyyd has a duplicate entry problem, therefore we only take on DTO per occuring title
                    // this way we could loose some episode entries that are actually different but have bad
                    // quality titles, but this is still better then to import us lots of triple episode
                    final Map<String, OldEpisode> map = new HashMap<>();
                    for (OldEpisode e : episodes) {
                       map.put(e.getTitle(), e);
                    }
                    return new LinkedList<>(map.values());
                }
            } else {
                log.info("JSON does not contain key 'episodes'");
            }
        } else {
            log.info("JSON does not contain key 'data'");
        }
        return new LinkedList<>();
    }

}

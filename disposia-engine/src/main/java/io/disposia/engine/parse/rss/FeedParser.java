package io.disposia.engine.parse.rss;

import io.disposia.engine.domain.Episode;
import io.disposia.engine.domain.Podcast;

import java.util.List;

public interface FeedParser {

    String NAMESPACE_ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd";
    String NAMESPACE_CONTENT = "http://purl.org/rss/1.0/modules/content/";
    String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
    String NAMESPACE_PSC = "http://podlove.org/simple-chapters";

    Podcast getPodcast();

    List<Episode> getEpisodes();

}

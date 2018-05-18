package echo.core.parse.rss;

import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface FeedParser {

    String NAMESPACE_ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd";
    String NAMESPACE_CONTENT = "http://purl.org/rss/1.0/modules/content/";
    String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
    String NAMESPACE_PSC = "http://podlove.org/simple-chapters";

    PodcastDTO getPodcast();

    List<EpisodeDTO> getEpisodes();

}

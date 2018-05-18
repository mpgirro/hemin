package echo.core.parse.rss;

import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maximilian Irro
 */
public class RomeFeedParserTest {

    private static final Logger log = LoggerFactory.getLogger(RomeFeedParserTest.class);

    private static String feedData;

    @BeforeAll
    static void setup() throws IOException {
        feedData = Files.lines(Paths.get("src","test","resources","testfeed.xml")).collect(Collectors.joining("\n"));
    }

    @AfterAll
    static void done() {

    }

    @BeforeEach
    void init() {

    }

    @AfterEach
    void tearDown() {

    }

    @DisplayName("All Podcast fields are not null or empty")
    @Test
    void test_podcastFieldAreParsed() throws FeedParsingException {
        final FeedParser parser = RomeFeedParser.of(feedData);
        final PodcastDTO p = parser.getPodcast();

        assertFalse(isNullOrEmpty(p.getTitle()), "<title> is null or empty");
        assertFalse(isNullOrEmpty(p.getLink()), "<link> is null or empty");
        assertFalse(isNullOrEmpty(p.getDescription()), "<description> is null or empty");
        assertFalse(isNullOrEmpty(p.getCopyright()), "<copyright> is null or empty");
        assertFalse(isNullOrEmpty(p.getDocs()), "<doc> is null or empty");
        assertFalse(isNullOrEmpty(p.getGenerator()), "<generator> is null or empty");
        assertFalse(isNullOrEmpty(p.getImage()), "<image> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesAuthor()), "<itunes:author> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesKeywords()), "<itunes:keywords> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesOwnerEmail()), "<itunes:owner><itunes:email> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesOwnerName()), "<itunes:owner><itunes:name> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesSummary()), "<itunes:summary> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesType()), "<itunes:type> is null or empty");
        assertNotNull(p.getItunesExplicit(), "<itunes:explicit> is null or empty");
        assertNotNull(p.getItunesBlock(), "<itunes:block> is null or empty");
        assertNotNull(p.getItunesCategories(), "<itunes:category>> is null");
        assertNotEquals(0, p.getItunesCategories().size());
        p.getItunesCategories().stream()
            .forEach(c -> assertFalse(isNullOrEmpty(c), "<itunes:category> is null or empty"));
    }

    @DisplayName("All Episodes are found")
    @Test
    void test_episodesEpisodesAreFound() throws FeedParsingException {
        final FeedParser parser = RomeFeedParser.of(feedData);
        assertEquals(2, parser.getEpisodes().size());
    }

    @DisplayName("All Episodes are found")
    @Test
    void test_episodeFieldsAreParsed() throws FeedParsingException {
        final FeedParser parser = RomeFeedParser.of(feedData);
        for (EpisodeDTO e : parser.getEpisodes()) {
            log.info(e.getGuid());
            assertFalse(isNullOrEmpty(e.getTitle()), "<title> is null or empty");
            assertFalse(isNullOrEmpty(e.getLink()), "<link> is null or empty");
            assertFalse(isNullOrEmpty(e.getDescription()), "<description> is null or empty");
            assertFalse(isNullOrEmpty(e.getGuid()), "<guid> is null or empty");
            assertNotNull(e.getGuidIsPermaLink(), "<guid isPermaLink> is null or empty");
            assertFalse(isNullOrEmpty(e.getImage()), "<image> is null or empty");
            assertFalse(isNullOrEmpty(e.getItunesAuthor()), "<itunes:author> is null or empty");
            assertFalse(isNullOrEmpty(e.getItunesDuration()), "<itunes:duration> is null or empty");
            assertFalse(isNullOrEmpty(e.getItunesSummary()), "<itunes:summary> is null or empty");
            assertFalse(isNullOrEmpty(e.getItunesSubtitle()), "<itunes:subtitle> is null or empty");
            assertFalse(isNullOrEmpty(e.getItunesEpisodeType()), "<itunes:episodeType> is null or empty");
            assertNotNull(e.getItunesSeason(), "<itunes:explicit> is null or empty");
            assertNotNull(e.getItunesEpisode(), "<itunes:block> is null or empty");
            assertNotNull(e.getEnclosureLength(), "<enclosure length> is null");
            assertFalse(isNullOrEmpty(e.getEnclosureType()), "<enclosure type> is null or empty");
            assertFalse(isNullOrEmpty(e.getEnclosureUrl()), "<enclosure url> is null or empty");
            assertFalse(isNullOrEmpty(e.getContentEncoded()), "<content:encoded> is null or empty");
            e.getChapters().stream()
                .forEach(c -> {
                    assertFalse(isNullOrEmpty(c.getTitle()), "<psc:chapter title> is null or empty");
                    assertFalse(isNullOrEmpty(c.getHref()), "<psc:chapter href> is null or empty");
                    assertFalse(isNullOrEmpty(c.getImage()), "<psc:chapter image> is null or empty");
                    assertFalse(isNullOrEmpty(c.getStart()), "<psc:chapter start> is null or empty");
                });
        }
    }

}

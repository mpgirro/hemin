package echo.core.index;

import echo.core.domain.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Maximilian Irro
 */
public class LuceneSearcherTest {

    private static final Logger log = LoggerFactory.getLogger(LuceneSearcherTest.class);

    private static final String INDEX_PATH = "./index";
    private static final boolean CREATE_INDEX = true; // will re-create index on every start (for testing)

    private static IndexCommitter committer;
    private static IndexSearcher searcher;

    @BeforeAll
    static void setup() throws IOException {

    }

    @AfterAll
    static void done() {

    }

    @BeforeEach
    void init() throws IOException {
        // we reset the index before each test
        committer = new LuceneCommitter(INDEX_PATH, CREATE_INDEX);
        searcher = new LuceneSearcher(((LuceneCommitter) committer).getIndexWriter());

    }

    @AfterEach
    void tearDown() {
        searcher.destroy();
        committer.destroy();
    }

    @DisplayName("Page parameter for search must be greater than 0")
    @Test
    void test_searchPageParamGreaterThanZero() {
        log.info("Testing if page parameter for search has to be greater than 0");
        final Executable searchExec = () -> searcher.search("test", 0, 1);
        assertThrows(SearchException.class, searchExec);
    }

    @DisplayName("Size parameter for search must be greater than 0")
    @Test
    void test_searchSizeParamGreaterThanZero() {
        log.info("Testing if size parameter for search has to be greater than 0");
        final Executable searchExec = () -> searcher.search("test", 1, 0);
        assertThrows(SearchException.class, searchExec);
    }

    @DisplayName("Query must not be null or empty")
    @Test
    void test_queryMustNotBeNullOrEmpty() throws SearchException {
        final Executable exec1 = () -> searcher.search(null, 1, 1);
        assertThrows(SearchException.class, exec1);

        final Executable exec2 = () -> searcher.search("", 1, 1);
        assertThrows(SearchException.class, exec2);
    }

    @DisplayName("Empty Result DTO if nothing was found")
    @Test
    void test_emptyResultDtoIfNothingFound() throws SearchException {
        log.info("Testing if the ResultWrapperDTO is empty if nothing was found");
        final ResultWrapperDTO result = searcher.search("test", 1, 1);
        assertEquals(0, result.getCurrPage().intValue());
        assertEquals(0, result.getMaxPage().intValue());
        assertEquals(0, result.getTotalHits().intValue());
        assertEquals(0, result.getResults().size());
    }

}

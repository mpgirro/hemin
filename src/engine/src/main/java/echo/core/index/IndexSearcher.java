package echo.core.index;

import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    ResultWrapperDTO search(String query, int page, int size) throws SearchException;

    Optional<IndexDocDTO> findByExo(String id) throws SearchException;

    void refresh();

    void destroy();

}

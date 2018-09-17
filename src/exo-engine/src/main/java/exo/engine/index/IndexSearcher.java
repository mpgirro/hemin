package exo.engine.index;

import exo.engine.domain.dto.IndexDocDTO;
import exo.engine.domain.dto.ResultWrapperDTO;
import exo.engine.exception.SearchException;

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

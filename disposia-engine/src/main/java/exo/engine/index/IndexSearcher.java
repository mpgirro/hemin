package exo.engine.index;

import exo.engine.domain.dto.IndexDoc;
import exo.engine.domain.dto.ResultWrapper;
import exo.engine.exception.SearchException;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
public interface IndexSearcher {

    ResultWrapper search(String query, int page, int size) throws SearchException;

    Optional<IndexDoc> findByExo(String id) throws SearchException;

    void refresh();

    void destroy();

}

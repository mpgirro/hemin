package io.disposia.engine.index;

import io.disposia.engine.domain.dto.IndexDoc;
import io.disposia.engine.domain.dto.ResultWrapper;
import io.disposia.engine.exception.SearchException;

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

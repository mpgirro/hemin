package io.disposia.engine.index;

import io.disposia.engine.domain.IndexDoc;
import io.disposia.engine.domain.ResultWrapper;
import io.disposia.engine.exception.SearchException;

import java.util.Optional;

public interface IndexSearcher {

    ResultWrapper search(String query, int page, int size) throws SearchException;

    Optional<IndexDoc> findByExo(String id) throws SearchException;

    void refresh();

    void destroy();

}

package io.disposia.engine.index;

import io.disposia.engine.olddomain.OldIndexDoc;
import io.disposia.engine.olddomain.OldResultWrapper;
import io.disposia.engine.exception.SearchException;

import java.util.Optional;

public interface IndexSearcher {

    OldResultWrapper search(String query, int page, int size) throws SearchException;

    Optional<OldIndexDoc> findById(String id) throws SearchException;

    void refresh();

    void destroy();

}

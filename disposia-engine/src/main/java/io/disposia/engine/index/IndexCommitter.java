package io.disposia.engine.index;


import io.disposia.engine.domain.Episode;
import io.disposia.engine.domain.IndexDoc;
import io.disposia.engine.domain.Podcast;
import io.disposia.engine.mapper.IndexMapper;

/**
 * This interface is used to standardize writing to search indizes.
 * Currently only Apache Lucene is supported, but this could be
 * extended to support Apache Solr or ElasticSearch as well.
 */
public interface IndexCommitter {

    void add(IndexDoc doc);

    default void add(Podcast podcast) {
        add(IndexMapper.INSTANCE.toModifiable(podcast));
    }

    default void add(Episode episode) {
        add(IndexMapper.INSTANCE.toModifiable(episode));
    }

    void update(IndexDoc doc);

    default void update(Podcast podcast) {
        update(IndexMapper.INSTANCE.toModifiable(podcast));
    }

    default void update(Episode episode) {
        update(IndexMapper.INSTANCE.toModifiable(episode));
    }

    void commit();

    void destroy();

}

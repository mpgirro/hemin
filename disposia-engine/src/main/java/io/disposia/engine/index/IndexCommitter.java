package io.disposia.engine.index;


import io.disposia.engine.mapper.OldIndexMapper;
import io.disposia.engine.olddomain.OldEpisode;
import io.disposia.engine.olddomain.OldIndexDoc;
import io.disposia.engine.olddomain.OldPodcast;

/**
 * This interface is used to standardize writing to search indizes.
 * Currently only Apache Lucene is supported, but this could be
 * extended to support Apache Solr or ElasticSearch as well.
 */
public interface IndexCommitter {

    void add(OldIndexDoc doc);

    default void add(OldPodcast podcast) {
        add(OldIndexMapper.INSTANCE.toModifiable(podcast));
    }

    default void add(OldEpisode episode) {
        add(OldIndexMapper.INSTANCE.toModifiable(episode));
    }

    void update(OldIndexDoc doc);

    default void update(OldPodcast podcast) {
        update(OldIndexMapper.INSTANCE.toModifiable(podcast));
    }

    default void update(OldEpisode episode) {
        update(OldIndexMapper.INSTANCE.toModifiable(episode));
    }

    void commit();

    void destroy();

}

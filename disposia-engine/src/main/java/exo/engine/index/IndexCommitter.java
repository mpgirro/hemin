package exo.engine.index;


import exo.engine.domain.dto.Episode;
import exo.engine.domain.dto.IndexDoc;
import exo.engine.domain.dto.Podcast;
import exo.engine.mapper.IndexMapper;

/**
 * This interface is used to standardize writing to search indizes.
 * Currently only Apache Lucene is supported, but this could be
 * extended to support Apache Solr or ElasticSearch as well.
 *
 * @author Maximilian Irro
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

package exo.engine.index;


import exo.engine.domain.dto.EpisodeDTO;
import exo.engine.domain.dto.IndexDocDTO;
import exo.engine.domain.dto.PodcastDTO;
import exo.engine.mapper.IndexMapper;

/**
 * This interface is used to standardize writing to search indizes.
 * Currently only Apache Lucene is supported, but this could be
 * extended to support Apache Solr or ElasticSearch as well.
 *
 * @author Maximilian Irro
 */
public interface IndexCommitter {

    void add(IndexDocDTO doc);

    default void add(PodcastDTO podcast) {
        add(IndexMapper.INSTANCE.toModifiable(podcast));
    }

    default void add(EpisodeDTO episode) {
        add(IndexMapper.INSTANCE.toModifiable(episode));
    }

    void update(IndexDocDTO doc);

    default void update(PodcastDTO podcast) {
        update(IndexMapper.INSTANCE.toModifiable(podcast));
    }

    default void update(EpisodeDTO episode) {
        update(IndexMapper.INSTANCE.toModifiable(episode));
    }

    void commit();

    void destroy();

}

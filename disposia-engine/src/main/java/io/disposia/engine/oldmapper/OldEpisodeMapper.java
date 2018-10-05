package io.disposia.engine.oldmapper;

import io.disposia.engine.domain.IndexField;
import io.disposia.engine.mapper.SolrFieldMapper;
import io.disposia.engine.olddomain.*;
import io.disposia.engine.olddomain.ImmutableOldEpisode;
import io.disposia.engine.olddomain.OldEpisode;
import org.apache.solr.common.SolrDocument;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Deprecated
@Mapper(uses = {OldPodcastMapper.class, OldChapterMapper.class, OldDateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OldEpisodeMapper {

    OldEpisodeMapper INSTANCE = Mappers.getMapper( OldEpisodeMapper.class );

    @Deprecated
    default ModifiableOldEpisode toModifiable(OldEpisode episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> {
                if (e instanceof ModifiableOldEpisode) {
                    return (ModifiableOldEpisode) e;
                } else {
                    return new ModifiableOldEpisode().from(e);
                }})
            .orElse(null);
    }

    @Deprecated
    default ImmutableOldEpisode toImmutable(OldEpisode episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> {
                if (e instanceof ImmutableOldEpisode) {
                    return (ImmutableOldEpisode) e;
                } else {
                    return ((ModifiableOldEpisode) e).toImmutable();
                }})
            .orElse(null);
    }

    @Deprecated
    ModifiableOldEpisode update(OldEpisode src, @MappingTarget ModifiableOldEpisode target);

    @Deprecated
    default ModifiableOldEpisode update(OldEpisode src, @MappingTarget OldEpisode target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldEpisode) {
                    return (ModifiableOldEpisode) t;
                } else {
                    return new ModifiableOldEpisode().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    @Deprecated
    default ImmutableOldEpisode updateImmutable(OldEpisode src, @MappingTarget OldEpisode target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldEpisode) {
                    return (ModifiableOldEpisode) t;
                } else {
                    return new ModifiableOldEpisode().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableOldEpisode::toImmutable)
            .orElse(null);
    }

    @Deprecated
    default ImmutableOldEpisode toImmutable(org.apache.lucene.document.Document doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutableOldEpisode.builder()
                .setId(d.get(IndexField.ID))
                //.setExo(d.get(IndexField.EXO))
                .setTitle(d.get(IndexField.TITLE))
                .setLink(d.get(IndexField.LINK))
                .setPubDate(Optional
                    .ofNullable(d.get(IndexField.PUB_DATE))
                    .map(OldDateMapper.INSTANCE::asLocalDateTime)
                    .orElse(null))
                .setDescription(Stream
                    .of(d.get(IndexField.ITUNES_SUMMARY), d.get(IndexField.DESCRIPTION))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null))
                .setImage(d.get(IndexField.ITUNES_IMAGE))
                .setItunesDuration(d.get(IndexField.ITUNES_DURATION))
                .setPodcastTitle(d.get(IndexField.PODCAST_TITLE))
                .create())
            .orElse(null);
    }

    @Deprecated
    default ImmutableOldEpisode toImmutable(SolrDocument doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutableOldEpisode.builder()
                .setId(SolrFieldMapper.INSTANCE.stringOrNull(d, IndexField.ID))
                //.setExo(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.EXO))
                .setTitle(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.TITLE))
                .setLink(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.LINK))
                .setPubDate(Optional
                    .ofNullable(SolrFieldMapper.INSTANCE.firstDateOrNull(d, IndexField.PUB_DATE))
                    .map(OldDateMapper.INSTANCE::asLocalDateTime)
                    .orElse(null))
                .setDescription(Stream
                    .of(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.ITUNES_SUMMARY), SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.DESCRIPTION))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null))
                .setImage(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.ITUNES_IMAGE))
                .setItunesDuration(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.ITUNES_DURATION))
                .setPodcastTitle(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.PODCAST_TITLE))
                .create())
            .orElse(null);
    }

}

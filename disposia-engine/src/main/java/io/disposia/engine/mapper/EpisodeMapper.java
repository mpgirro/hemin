package io.disposia.engine.mapper;

import io.disposia.engine.domain.IndexField;
import io.disposia.engine.domain.Episode;
import io.disposia.engine.domain.ImmutableEpisode;
import io.disposia.engine.domain.ModifiableEpisode;
import org.apache.solr.common.SolrDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Mapper(uses = {PodcastMapper.class, ChapterMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface EpisodeMapper {

    EpisodeMapper INSTANCE = Mappers.getMapper( EpisodeMapper.class );

    default ModifiableEpisode toModifiable(Episode episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> {
                if (e instanceof ModifiableEpisode) {
                    return (ModifiableEpisode) e;
                } else {
                    return new ModifiableEpisode().from(e);
                }})
            .orElse(null);
    }

    default ImmutableEpisode toImmutable(Episode episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> {
                if (e instanceof ImmutableEpisode) {
                    return (ImmutableEpisode) e;
                } else {
                    return ((ModifiableEpisode) e).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableEpisode update(Episode src, @MappingTarget ModifiableEpisode target);

    default ModifiableEpisode update(Episode src, @MappingTarget Episode target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableEpisode) {
                    return (ModifiableEpisode) t;
                } else {
                    return new ModifiableEpisode().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableEpisode updateImmutable(Episode src, @MappingTarget Episode target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableEpisode) {
                    return (ModifiableEpisode) t;
                } else {
                    return new ModifiableEpisode().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableEpisode::toImmutable)
            .orElse(null);
    }

    default ImmutableEpisode toImmutable(org.apache.lucene.document.Document doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutableEpisode.builder()
                .setExo(d.get(IndexField.EXO))
                .setTitle(d.get(IndexField.TITLE))
                .setLink(d.get(IndexField.LINK))
                .setPubDate(Optional
                    .ofNullable(d.get(IndexField.PUB_DATE))
                    .map(DateMapper.INSTANCE::asLocalDateTime)
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

    default ImmutableEpisode toImmutable(SolrDocument doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutableEpisode.builder()
                .setExo((String) d.getFieldValue(IndexField.EXO))
                .setTitle((String) d.getFieldValue(IndexField.TITLE))
                .setLink((String) d.getFieldValue(IndexField.LINK))
                .setPubDate(Optional
                    .ofNullable(d.getFieldValue(IndexField.PUB_DATE))
                    .map(o -> (String) o)
                    .map(DateMapper.INSTANCE::asLocalDateTime)
                    .orElse(null))
                .setDescription(Stream
                    .of(d.getFieldValue(IndexField.ITUNES_SUMMARY), d.getFieldValue(IndexField.DESCRIPTION))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(o -> (String) o)
                    .orElse(null))
                .setImage((String) d.getFieldValue(IndexField.ITUNES_IMAGE))
                .setItunesDuration((String) d.getFieldValue(IndexField.ITUNES_DURATION))
                .setPodcastTitle((String) d.getFieldValue(IndexField.PODCAST_TITLE))
                .create())
            .orElse(null);
    }

}

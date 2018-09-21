package io.disposia.engine.mapper;

import io.disposia.engine.domain.IndexField;
import io.disposia.engine.domain.dto.Episode;
import io.disposia.engine.domain.dto.ImmutableEpisode;
import io.disposia.engine.domain.dto.ModifiableEpisode;
import io.disposia.engine.domain.entity.EpisodeEntity;
import io.disposia.engine.domain.entity.PodcastEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Maximilian Irro
 */
@Mapper(uses = {PodcastMapper.class, ChapterMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface EpisodeMapper {

    EpisodeMapper INSTANCE = Mappers.getMapper( EpisodeMapper.class );

    @Mapping(source = "podcastId", target = "podcast")
    @Mapping(target = "chapters", ignore = true) // TODO this make DB persist calls fail
    EpisodeEntity toEntity(Episode episode);

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.exo", target = "podcastExo")
    @Mapping(source = "podcast.title", target = "podcastTitle")
    @Mapping(target = "chapters", ignore = true) // TODO this make DB persist calls fail
    ModifiableEpisode toModifiable(EpisodeEntity episode);

    default ImmutableEpisode toImmutable(EpisodeEntity episode) {
        return Optional
            .ofNullable(episode)
            .map(this::toModifiable)
            .map(ModifiableEpisode::toImmutable)
            .orElse(null);
    }

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

    default PodcastEntity podcastFromId(Long podcastId) {
        return Optional
            .ofNullable(podcastId)
            .map(id -> {
                final PodcastEntity p = new PodcastEntity();
                p.setId(id);
                return p;
            })
            .orElse(null);
    }

}

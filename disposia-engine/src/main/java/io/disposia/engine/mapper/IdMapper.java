package io.disposia.engine.mapper;

import io.disposia.engine.domain.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

/**
 * This mapper cleans all database IDs by setting them to null. Database IDs are only
 * valid in a local scope, and without ensuring they are whiped before sending a DTO
 * to a remote scope could cause to confusion and invalid databases.
 *
 * Identification in a global scope is done by using the EXO (= external ID) value,
 * which this mapper leaves untouched.
 *
 * @author Maximilian Irro
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface IdMapper {

    IdMapper INSTANCE = Mappers.getMapper( IdMapper.class );

    @Mapping(target = "id", ignore = true)
    ModifiablePodcast clearModifiable(Podcast podcast);

    default ImmutablePodcast clearImmutable(Podcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(this::clearModifiable)
            .map(ModifiablePodcast::toImmutable)
            .orElse(null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableEpisode clearModifiable(Episode episode);

    default ImmutableEpisode clearImmutable(Episode episode) {
        return Optional
            .ofNullable(episode)
            .map(this::clearModifiable)
            .map(ModifiableEpisode::toImmutable)
            .orElse(null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableFeed clearModifiable(Feed feed);

    default ImmutableFeed clearImmutable(Feed feed) {
        return Optional
            .ofNullable(feed)
            .map(this::clearModifiable)
            .map(ModifiableFeed::toImmutable)
            .orElse(null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "episodeId", ignore = true)
    ModifiableChapter clearModifiable(Chapter chapter);

    default ImmutableChapter clearImmutable(Chapter chapter) {
        return Optional
            .ofNullable(chapter)
            .map(this::clearModifiable)
            .map(ModifiableChapter::toImmutable)
            .orElse(null);
    }

}

package io.disposia.engine.mapper;

import io.disposia.engine.olddomain.OldChapter;
import io.disposia.engine.olddomain.OldEpisode;
import io.disposia.engine.olddomain.OldFeed;
import io.disposia.engine.olddomain.OldPodcast;
import io.disposia.engine.olddomain.*;
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
 */
@Deprecated
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OldIdMapper {

    OldIdMapper INSTANCE = Mappers.getMapper( OldIdMapper.class );

    @Deprecated
    @Mapping(target = "id", ignore = true)
    ModifiableOldPodcast clearModifiable(OldPodcast podcast);

    @Deprecated
    default ImmutableOldPodcast clearImmutable(OldPodcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(this::clearModifiable)
            .map(ModifiableOldPodcast::toImmutable)
            .orElse(null);
    }

    @Deprecated
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableOldEpisode clearModifiable(OldEpisode episode);

    @Deprecated
    default ImmutableOldEpisode clearImmutable(OldEpisode episode) {
        return Optional
            .ofNullable(episode)
            .map(this::clearModifiable)
            .map(ModifiableOldEpisode::toImmutable)
            .orElse(null);
    }

    @Deprecated
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableOldFeed clearModifiable(OldFeed feed);

    default ImmutableOldFeed clearImmutable(OldFeed feed) {
        return Optional
            .ofNullable(feed)
            .map(this::clearModifiable)
            .map(ModifiableOldFeed::toImmutable)
            .orElse(null);
    }

    @Deprecated
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "episodeId", ignore = true)
    ModifiableOldChapter clearModifiable(OldChapter chapter);

    @Deprecated
    default ImmutableOldChapter clearImmutable(OldChapter chapter) {
        return Optional
            .ofNullable(chapter)
            .map(this::clearModifiable)
            .map(ModifiableOldChapter::toImmutable)
            .orElse(null);
    }

}

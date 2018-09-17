package exo.engine.mapper;

import exo.engine.domain.dto.*;
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
    ModifiablePodcastDTO clearModifiable(PodcastDTO podcast);

    default ImmutablePodcastDTO clearImmutable(PodcastDTO podcast) {
        return Optional
            .ofNullable(podcast)
            .map(this::clearModifiable)
            .map(ModifiablePodcastDTO::toImmutable)
            .orElse(null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableEpisodeDTO clearModifiable(EpisodeDTO episode);

    default ImmutableEpisodeDTO clearImmutable(EpisodeDTO episode) {
        return Optional
            .ofNullable(episode)
            .map(this::clearModifiable)
            .map(ModifiableEpisodeDTO::toImmutable)
            .orElse(null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableFeedDTO clearModifiable(FeedDTO feed);

    default ImmutableFeedDTO clearImmutable(FeedDTO feed) {
        return Optional
            .ofNullable(feed)
            .map(this::clearModifiable)
            .map(ModifiableFeedDTO::toImmutable)
            .orElse(null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "episodeId", ignore = true)
    ModifiableChapterDTO clearModifiable(ChapterDTO chapter);

    default ImmutableChapterDTO clearImmutable(ChapterDTO chapter) {
        return Optional
            .ofNullable(chapter)
            .map(this::clearModifiable)
            .map(ModifiableChapterDTO::toImmutable)
            .orElse(null);
    }

}

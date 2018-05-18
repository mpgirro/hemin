package echo.core.mapper;

import echo.core.domain.dto.FeedDTO;
import echo.core.domain.dto.ImmutableFeedDTO;
import echo.core.domain.dto.ModifiableFeedDTO;
import echo.core.domain.entity.FeedEntity;
import echo.core.domain.entity.PodcastEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Mapper(uses = {PodcastMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    @Mapping(source = "podcastId", target = "podcast")
    FeedEntity toEntity(FeedDTO feed);

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.exo", target = "podcastExo")
    ModifiableFeedDTO toModifiable(FeedEntity feed);

    default ImmutableFeedDTO toImmutable(FeedEntity feed) {
        return Optional
            .ofNullable(feed)
            .map(this::toModifiable)
            .map(ModifiableFeedDTO::toImmutable)
            .orElse(null);
    }

    default ModifiableFeedDTO toModifiable(FeedDTO feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> {
                if (f instanceof ModifiableFeedDTO) {
                    return (ModifiableFeedDTO) f;
                } else {
                    return new ModifiableFeedDTO().from(f);
                }})
            .orElse(null);
    }

    default ImmutableFeedDTO toImmutable(FeedDTO feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> {
                if (f instanceof ImmutableFeedDTO) {
                    return (ImmutableFeedDTO) f;
                } else {
                    return ((ModifiableFeedDTO) f).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableFeedDTO update(FeedDTO src, @MappingTarget ModifiableFeedDTO target);

    default ModifiableFeedDTO update(FeedDTO src, @MappingTarget FeedDTO target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableFeedDTO) {
                    return (ModifiableFeedDTO) t;
                } else {
                    return new ModifiableFeedDTO().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableFeedDTO updateImmutable(FeedDTO src, @MappingTarget FeedDTO target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableFeedDTO) {
                    return (ModifiableFeedDTO) t;
                } else {
                    return new ModifiableFeedDTO().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableFeedDTO::toImmutable)
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

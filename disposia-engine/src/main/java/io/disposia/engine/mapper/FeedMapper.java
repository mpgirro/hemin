package io.disposia.engine.mapper;

import io.disposia.engine.domain.dto.Feed;
import io.disposia.engine.domain.dto.ImmutableFeed;
import io.disposia.engine.domain.dto.ModifiableFeed;
import io.disposia.engine.domain.entity.FeedEntity;
import io.disposia.engine.domain.entity.PodcastEntity;
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
    FeedEntity toEntity(Feed feed);

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.exo", target = "podcastExo")
    ModifiableFeed toModifiable(FeedEntity feed);

    default ImmutableFeed toImmutable(FeedEntity feed) {
        return Optional
            .ofNullable(feed)
            .map(this::toModifiable)
            .map(ModifiableFeed::toImmutable)
            .orElse(null);
    }

    default ModifiableFeed toModifiable(Feed feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> {
                if (f instanceof ModifiableFeed) {
                    return (ModifiableFeed) f;
                } else {
                    return new ModifiableFeed().from(f);
                }})
            .orElse(null);
    }

    default ImmutableFeed toImmutable(Feed feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> {
                if (f instanceof ImmutableFeed) {
                    return (ImmutableFeed) f;
                } else {
                    return ((ModifiableFeed) f).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableFeed update(Feed src, @MappingTarget ModifiableFeed target);

    default ModifiableFeed update(Feed src, @MappingTarget Feed target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableFeed) {
                    return (ModifiableFeed) t;
                } else {
                    return new ModifiableFeed().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableFeed updateImmutable(Feed src, @MappingTarget Feed target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableFeed) {
                    return (ModifiableFeed) t;
                } else {
                    return new ModifiableFeed().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableFeed::toImmutable)
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

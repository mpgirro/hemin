package io.disposia.engine.mapper;

import io.disposia.engine.olddomain.ImmutableOldFeed;
import io.disposia.engine.olddomain.ModifiableOldFeed;
import io.disposia.engine.olddomain.OldFeed;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

@Mapper(uses = {PodcastMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    default ModifiableOldFeed toModifiable(OldFeed feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> {
                if (f instanceof ModifiableOldFeed) {
                    return (ModifiableOldFeed) f;
                } else {
                    return new ModifiableOldFeed().from(f);
                }})
            .orElse(null);
    }

    default ImmutableOldFeed toImmutable(OldFeed feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> {
                if (f instanceof ImmutableOldFeed) {
                    return (ImmutableOldFeed) f;
                } else {
                    return ((ModifiableOldFeed) f).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableOldFeed update(OldFeed src, @MappingTarget ModifiableOldFeed target);

    default ModifiableOldFeed update(OldFeed src, @MappingTarget OldFeed target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldFeed) {
                    return (ModifiableOldFeed) t;
                } else {
                    return new ModifiableOldFeed().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableOldFeed updateImmutable(OldFeed src, @MappingTarget OldFeed target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldFeed) {
                    return (ModifiableOldFeed) t;
                } else {
                    return new ModifiableOldFeed().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableOldFeed::toImmutable)
            .orElse(null);
    }

}

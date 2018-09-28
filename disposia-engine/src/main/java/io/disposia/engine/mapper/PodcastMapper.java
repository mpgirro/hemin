package io.disposia.engine.mapper;

import io.disposia.engine.domain.IndexField;
import io.disposia.engine.domain.ImmutablePodcast;
import io.disposia.engine.domain.ModifiablePodcast;
import io.disposia.engine.domain.Podcast;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Mapper(
    uses = {UrlMapper.class, DateMapper.class},
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface PodcastMapper {

    PodcastMapper INSTANCE = Mappers.getMapper( PodcastMapper.class );

    default ModifiablePodcast toModifiable(Podcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(p -> {
                if (p instanceof ModifiablePodcast) {
                    return (ModifiablePodcast) p;
                } else {
                    return new ModifiablePodcast().from(p);
                }})
            .orElse(null);
    }

    default ImmutablePodcast toImmutable(Podcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(p -> {
                if (p instanceof ImmutablePodcast) {
                    return (ImmutablePodcast) p;
                } else {
                    return ((ModifiablePodcast) p).toImmutable();
                }})
            .orElse(null);
    }

    ModifiablePodcast update(Podcast src, @MappingTarget ModifiablePodcast target);

    default ModifiablePodcast update(Podcast src, @MappingTarget Podcast target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiablePodcast) {
                    return (ModifiablePodcast) t;
                } else {
                    return new ModifiablePodcast().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutablePodcast updateImmutable(Podcast src, @MappingTarget Podcast target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiablePodcast) {
                    return (ModifiablePodcast) t;
                } else {
                    return new ModifiablePodcast().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiablePodcast::toImmutable)
            .orElse(null);
    }

    default ImmutablePodcast toImmutable(org.apache.lucene.document.Document doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutablePodcast.builder()
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
                .create())
            .orElse(null);
    }

}

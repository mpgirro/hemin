package io.disposia.engine.mapper;

import io.disposia.engine.domain.IndexField;
import io.disposia.engine.olddomain.*;
import io.disposia.engine.olddomain.ModifiableOldPodcast;
import io.disposia.engine.olddomain.OldPodcast;
import org.apache.solr.common.SolrDocument;
import org.mapstruct.Mapper;
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

    default ModifiableOldPodcast toModifiable(OldPodcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(p -> {
                if (p instanceof ModifiableOldPodcast) {
                    return (ModifiableOldPodcast) p;
                } else {
                    return new ModifiableOldPodcast().from(p);
                }})
            .orElse(null);
    }

    default ImmutableOldPodcast toImmutable(OldPodcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(p -> {
                if (p instanceof ImmutableOldPodcast) {
                    return (ImmutableOldPodcast) p;
                } else {
                    return ((ModifiableOldPodcast) p).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableOldPodcast update(OldPodcast src, @MappingTarget ModifiableOldPodcast target);

    default ModifiableOldPodcast update(OldPodcast src, @MappingTarget OldPodcast target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldPodcast) {
                    return (ModifiableOldPodcast) t;
                } else {
                    return new ModifiableOldPodcast().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableOldPodcast updateImmutable(OldPodcast src, @MappingTarget OldPodcast target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldPodcast) {
                    return (ModifiableOldPodcast) t;
                } else {
                    return new ModifiableOldPodcast().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableOldPodcast::toImmutable)
            .orElse(null);
    }

    default ImmutableOldPodcast toImmutable(org.apache.lucene.document.Document doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutableOldPodcast.builder()
                .setId(d.get(IndexField.ID))
                //.setExo(d.get(IndexField.EXO))
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

    default ImmutableOldPodcast toImmutable(SolrDocument doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutableOldPodcast.builder()
                .setId(SolrFieldMapper.INSTANCE.stringOrNull(d, IndexField.ID))
                //.setExo(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.EXO))
                .setTitle(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.TITLE))
                .setLink(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.LINK))
                .setPubDate(Optional
                    .ofNullable(SolrFieldMapper.INSTANCE.firstDateOrNull(d, IndexField.PUB_DATE))
                    .map(DateMapper.INSTANCE::asLocalDateTime)
                    .orElse(null))
                .setDescription(Stream
                    .of(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.ITUNES_SUMMARY), SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.DESCRIPTION))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null))
                .setImage(SolrFieldMapper.INSTANCE.firstStringOrNull(d, IndexField.ITUNES_IMAGE))
                .create())
            .orElse(null);
    }

}

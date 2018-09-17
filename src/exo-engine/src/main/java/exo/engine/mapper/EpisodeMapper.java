package exo.engine.mapper;

import exo.engine.domain.IndexField;
import exo.engine.domain.dto.EpisodeDTO;
import exo.engine.domain.dto.ImmutableEpisodeDTO;
import exo.engine.domain.dto.ModifiableEpisodeDTO;
import exo.engine.domain.entity.EpisodeEntity;
import exo.engine.domain.entity.PodcastEntity;
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
    EpisodeEntity toEntity(EpisodeDTO episode);

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.exo", target = "podcastExo")
    @Mapping(source = "podcast.title", target = "podcastTitle")
    @Mapping(target = "chapters", ignore = true) // TODO this make DB persist calls fail
    ModifiableEpisodeDTO toModifiable(EpisodeEntity episode);

    default ImmutableEpisodeDTO toImmutable(EpisodeEntity episode) {
        return Optional
            .ofNullable(episode)
            .map(this::toModifiable)
            .map(ModifiableEpisodeDTO::toImmutable)
            .orElse(null);
    }

    default ModifiableEpisodeDTO toModifiable(EpisodeDTO episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> {
                if (e instanceof ModifiableEpisodeDTO) {
                    return (ModifiableEpisodeDTO) e;
                } else {
                    return new ModifiableEpisodeDTO().from(e);
                }})
            .orElse(null);
    }

    default ImmutableEpisodeDTO toImmutable(EpisodeDTO episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> {
                if (e instanceof ImmutableEpisodeDTO) {
                    return (ImmutableEpisodeDTO) e;
                } else {
                    return ((ModifiableEpisodeDTO) e).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableEpisodeDTO update(EpisodeDTO src, @MappingTarget ModifiableEpisodeDTO target);

    default ModifiableEpisodeDTO update(EpisodeDTO src, @MappingTarget EpisodeDTO target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableEpisodeDTO) {
                    return (ModifiableEpisodeDTO) t;
                } else {
                    return new ModifiableEpisodeDTO().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableEpisodeDTO updateImmutable(EpisodeDTO src, @MappingTarget EpisodeDTO target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableEpisodeDTO) {
                    return (ModifiableEpisodeDTO) t;
                } else {
                    return new ModifiableEpisodeDTO().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableEpisodeDTO::toImmutable)
            .orElse(null);
    }

    default ImmutableEpisodeDTO toImmutable(org.apache.lucene.document.Document doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> ImmutableEpisodeDTO.builder()
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

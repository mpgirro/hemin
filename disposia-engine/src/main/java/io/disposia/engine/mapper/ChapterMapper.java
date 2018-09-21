package io.disposia.engine.mapper;

import io.disposia.engine.domain.dto.ImmutableChapter;
import io.disposia.engine.domain.dto.ModifiableChapter;
import io.disposia.engine.domain.dto.Chapter;
import io.disposia.engine.domain.entity.ChapterEntity;
import io.disposia.engine.domain.entity.EpisodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Mapper(uses = {EpisodeMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ChapterMapper {

    ChapterMapper INSTANCE = Mappers.getMapper( ChapterMapper.class );

    @Mapping(source = "episodeId", target = "episode")
    ChapterEntity toEntity(Chapter chapter);

    @Mapping(source = "episode.id", target = "episodeId")
    @Mapping(source = "episode.exo", target = "episodeExo")
    ModifiableChapter toModifiable(ChapterEntity chapter);

    default ModifiableChapter toModifiable(Chapter chapter) {
        return Optional
            .ofNullable(chapter)
            .map(c -> {
                if (c instanceof ModifiableChapter) {
                    return (ModifiableChapter) c;
                } else {
                    return new ModifiableChapter().from(c);
                }})
            .orElse(null);
    }

    default ImmutableChapter toImmutable(Chapter chapter) {
        return Optional
            .ofNullable(chapter)
            .map(c -> {
                if (c instanceof ImmutableChapter) {
                    return (ImmutableChapter) c;
                } else {
                    return ((ModifiableChapter) c).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableChapter update(Chapter src, @MappingTarget ModifiableChapter target);

    default ModifiableChapter update(Chapter src, @MappingTarget Chapter target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableChapter) {
                    return (ModifiableChapter) t;
                } else {
                    return new ModifiableChapter().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableChapter updateImmutable(Chapter src, @MappingTarget Chapter target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableChapter) {
                    return (ModifiableChapter) t;
                } else {
                    return new ModifiableChapter().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableChapter::toImmutable)
            .orElse(null);
    }

    default EpisodeEntity episodeFromId(Long episodeId) {
        return Optional
            .ofNullable(episodeId)
            .map(id -> {
                final EpisodeEntity e = new EpisodeEntity();
                e.setId(id);
                return e;
            })
            .orElse(null);
    }

}

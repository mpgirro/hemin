package echo.core.mapper;

import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.ImmutableChapterDTO;
import echo.core.domain.dto.ModifiableChapterDTO;
import echo.core.domain.entity.ChapterEntity;
import echo.core.domain.entity.EpisodeEntity;
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
    ChapterEntity toEntity(ChapterDTO chapter);

    @Mapping(source = "episode.id", target = "episodeId")
    @Mapping(source = "episode.exo", target = "episodeExo")
    ModifiableChapterDTO toModifiable(ChapterEntity chapter);

    default ModifiableChapterDTO toModifiable(ChapterDTO chapter) {
        return Optional
            .ofNullable(chapter)
            .map(c -> {
                if (c instanceof ModifiableChapterDTO) {
                    return (ModifiableChapterDTO) c;
                } else {
                    return new ModifiableChapterDTO().from(c);
                }})
            .orElse(null);
    }

    default ImmutableChapterDTO toImmutable(ChapterDTO chapter) {
        return Optional
            .ofNullable(chapter)
            .map(c -> {
                if (c instanceof ImmutableChapterDTO) {
                    return (ImmutableChapterDTO) c;
                } else {
                    return ((ModifiableChapterDTO) c).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableChapterDTO update(ChapterDTO src, @MappingTarget ModifiableChapterDTO target);

    default ModifiableChapterDTO update(ChapterDTO src, @MappingTarget ChapterDTO target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableChapterDTO) {
                    return (ModifiableChapterDTO) t;
                } else {
                    return new ModifiableChapterDTO().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableChapterDTO updateImmutable(ChapterDTO src, @MappingTarget ChapterDTO target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableChapterDTO) {
                    return (ModifiableChapterDTO) t;
                } else {
                    return new ModifiableChapterDTO().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableChapterDTO::toImmutable)
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

package io.disposia.engine.mapper;

import io.disposia.engine.olddomain.ImmutableOldChapter;
import io.disposia.engine.olddomain.ModifiableOldChapter;
import io.disposia.engine.olddomain.OldChapter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

@Mapper(uses = {EpisodeMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ChapterMapper {

    ChapterMapper INSTANCE = Mappers.getMapper( ChapterMapper.class );

    default ModifiableOldChapter toModifiable(OldChapter chapter) {
        return Optional
            .ofNullable(chapter)
            .map(c -> {
                if (c instanceof ModifiableOldChapter) {
                    return (ModifiableOldChapter) c;
                } else {
                    return new ModifiableOldChapter().from(c);
                }})
            .orElse(null);
    }

    default ImmutableOldChapter toImmutable(OldChapter chapter) {
        return Optional
            .ofNullable(chapter)
            .map(c -> {
                if (c instanceof ImmutableOldChapter) {
                    return (ImmutableOldChapter) c;
                } else {
                    return ((ModifiableOldChapter) c).toImmutable();
                }})
            .orElse(null);
    }

    ModifiableOldChapter update(OldChapter src, @MappingTarget ModifiableOldChapter target);

    default ModifiableOldChapter update(OldChapter src, @MappingTarget OldChapter target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldChapter) {
                    return (ModifiableOldChapter) t;
                } else {
                    return new ModifiableOldChapter().from(t);
                }})
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableOldChapter updateImmutable(OldChapter src, @MappingTarget OldChapter target) {
        return Optional
            .ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableOldChapter) {
                    return (ModifiableOldChapter) t;
                } else {
                    return new ModifiableOldChapter().from(t);
                }})
            .map(t -> update(src, t))
            .map(ModifiableOldChapter::toImmutable)
            .orElse(null);
    }

}

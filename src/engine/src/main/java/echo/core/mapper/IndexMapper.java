package echo.core.mapper;

import echo.core.domain.dto.*;
import echo.core.index.IndexField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
@Mapper(uses = {PodcastMapper.class, EpisodeMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface IndexMapper {

    IndexMapper INSTANCE = Mappers.getMapper( IndexMapper.class );

    @Mapping(target = "docType", constant = "podcast")
    @Mapping(target = "podcastTitle", ignore = true)
    @Mapping(target = "chapterMarks", ignore = true)
    @Mapping(target = "contentEncoded", ignore = true)
    @Mapping(target = "transcript", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    ModifiableIndexDocDTO toModifiable(PodcastDTO podcast);

    @Mapping(target = "docType", constant = "episode")
    @Mapping(source = "chapters", target = "chapterMarks")
    @Mapping(target = "itunesSummary", ignore = true)
    @Mapping(target = "transcript", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    ModifiableIndexDocDTO toModifiable(EpisodeDTO episodeDTO);

    default ImmutableIndexDocDTO toImmutable(PodcastDTO podcast) {
        return Optional
            .ofNullable(podcast)
            .map(this::toModifiable)
            .map(ModifiableIndexDocDTO::toImmutable)
            .orElse(null);
    }

    default ImmutableIndexDocDTO toImmutable(EpisodeDTO episode) {
        return Optional
            .ofNullable(episode)
            .map(this::toModifiable)
            .map(ModifiableIndexDocDTO::toImmutable)
            .orElse(null);
    }

    default String map(List<ChapterDTO> chapters){
        return Optional
            .ofNullable(chapters)
            .map(cs -> String.join("\n", cs.stream()
                .map(ChapterDTO::getTitle)
                .collect(Collectors.toList())))
            .orElse(null);
    }

    default ModifiableIndexDocDTO toModifiable(IndexDocDTO doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> {
                if (doc instanceof ModifiableIndexDocDTO) {
                    return (ModifiableIndexDocDTO) d;
                } else {
                    return new ModifiableIndexDocDTO().from(d);
                }})
            .orElse(null);
    }

    default ImmutableIndexDocDTO toImmutable(IndexDocDTO doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> {
                if (d instanceof ImmutableIndexDocDTO) {
                    return (ImmutableIndexDocDTO) d;
                } else {
                    return ((ModifiableIndexDocDTO) d).toImmutable();
                }})
            .orElse(null);
    }

    default ImmutableIndexDocDTO toImmutable(Document doc) {

        if (doc == null) return null;

        switch (doc.get(IndexField.DOC_TYPE)) {
            case "podcast": return toImmutable(PodcastMapper.INSTANCE.toImmutable(doc));
            case "episode": return toImmutable(EpisodeMapper.INSTANCE.toImmutable(doc));
            default: throw new RuntimeException("Unsupported lucene document type : " + doc.get(IndexField.DOC_TYPE));
        }
    }

    default Document toLucene(IndexDocDTO doc) {

        if (doc == null) return null;

        final Document lucene = new Document();

        Optional.ofNullable(doc.getDocType())
            .ifPresent(value -> lucene.add(new StringField(IndexField.DOC_TYPE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getExo())
            .ifPresent(value -> lucene.add(new StringField(IndexField.EXO, value, Field.Store.YES)));
        Optional.ofNullable(doc.getTitle())
            .ifPresent(value -> lucene.add(new TextField(IndexField.TITLE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getLink())
            .ifPresent(value -> lucene.add(new TextField(IndexField.LINK, value, Field.Store.YES)));
        Optional.ofNullable(doc.getDescription())
            .ifPresent(value -> lucene.add(new TextField(IndexField.DESCRIPTION, value, Field.Store.YES)));
        Optional.ofNullable(doc.getPodcastTitle())
            .ifPresent(value -> lucene.add(new TextField(IndexField.PODCAST_TITLE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getPubDate())
            .map(DateMapper.INSTANCE::asString)
            .ifPresent(value -> lucene.add(new StringField(IndexField.PUB_DATE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getImage())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_IMAGE, value, Field.Store.YES)));
        Optional.ofNullable(doc.getItunesAuthor())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_AUTHOR, value, Field.Store.NO)));
        Optional.ofNullable(doc.getItunesSummary())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_SUMMARY, value, Field.Store.YES)));
        Optional.ofNullable(doc.getChapterMarks())
            .ifPresent(value -> lucene.add(new TextField(IndexField.CHAPTER_MARKS, value, Field.Store.NO)));
        Optional.ofNullable(doc.getContentEncoded())
            .ifPresent(value -> lucene.add(new TextField(IndexField.CONTENT_ENCODED, value, Field.Store.NO)));
        Optional.ofNullable(doc.getTranscript())
            .ifPresent(value -> lucene.add(new TextField(IndexField.TRANSCRIPT, value, Field.Store.NO)));
        Optional.ofNullable(doc.getWebsiteData())
            .ifPresent(value -> lucene.add(new TextField(IndexField.WEBSITE_DATA, value, Field.Store.NO)));

        return lucene;
    }
}

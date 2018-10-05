package io.disposia.engine.mapper;

import io.disposia.engine.domain.IndexField;
import io.disposia.engine.olddomain.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.solr.common.SolrDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

@Deprecated
@Mapper(uses = {OldPodcastMapper.class, OldEpisodeMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OldIndexMapper {

    OldIndexMapper INSTANCE = Mappers.getMapper( OldIndexMapper.class );

    @Deprecated
    @Mapping(target = "docType", constant = "podcast")
    @Mapping(target = "podcastTitle", ignore = true)
    @Mapping(target = "chapterMarks", ignore = true)
    @Mapping(target = "contentEncoded", ignore = true)
    @Mapping(target = "transcript", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    ModifiableOldIndexDoc toModifiable(OldPodcast podcast);

    @Deprecated
    @Mapping(target = "docType", constant = "episode")
    @Mapping(source = "chapters", target = "chapterMarks")
    @Mapping(target = "itunesSummary", ignore = true)
    @Mapping(target = "transcript", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    ModifiableOldIndexDoc toModifiable(OldEpisode episode);

    @Deprecated
    default ImmutableOldIndexDoc toImmutable(OldPodcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(this::toModifiable)
            .map(ModifiableOldIndexDoc::toImmutable)
            .orElse(null);
    }

    @Deprecated
    default ImmutableOldIndexDoc toImmutable(OldEpisode episode) {
        return Optional
            .ofNullable(episode)
            .map(this::toModifiable)
            .map(ModifiableOldIndexDoc::toImmutable)
            .orElse(null);
    }

    @Deprecated
    default String map(List<OldChapter> chapters){
        return Optional
            .ofNullable(chapters)
            .map(cs -> String.join("\n", cs.stream()
                .map(OldChapter::getTitle)
                .collect(Collectors.toList())))
            .orElse(null);
    }

    @Deprecated
    default ModifiableOldIndexDoc toModifiable(OldIndexDoc doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> {
                if (doc instanceof ModifiableOldIndexDoc) {
                    return (ModifiableOldIndexDoc) d;
                } else {
                    return new ModifiableOldIndexDoc().from(d);
                }})
            .orElse(null);
    }

    @Deprecated
    default ImmutableOldIndexDoc toImmutable(OldIndexDoc doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> {
                if (d instanceof ImmutableOldIndexDoc) {
                    return (ImmutableOldIndexDoc) d;
                } else {
                    return ((ModifiableOldIndexDoc) d).toImmutable();
                }})
            .orElse(null);
    }

    @Deprecated
    default ImmutableOldIndexDoc toImmutable(org.apache.lucene.document.Document doc) {

        if (doc == null) return null;

        final String docType = doc.get(IndexField.DOC_TYPE);
        if (isNullOrEmpty(docType)) {
            throw new RuntimeException("Document type is required but found NULL");
        }

        switch (docType) {
            case "podcast": return toImmutable(OldPodcastMapper.INSTANCE.toImmutable(doc));
            case "episode": return toImmutable(OldEpisodeMapper.INSTANCE.toImmutable(doc));
            default: throw new RuntimeException("Unsupported document type : " + docType);
        }
    }

    @Deprecated
    default ImmutableOldIndexDoc toImmutable(SolrDocument doc) {

        if (doc == null) return null;

        final String docType = SolrFieldMapper.INSTANCE.firstStringOrNull(doc, IndexField.DOC_TYPE);
        if (isNullOrEmpty(docType)) {
            throw new RuntimeException("Document type is required but found NULL");
        }

        switch (docType) {
            case "podcast": return toImmutable(OldPodcastMapper.INSTANCE.toImmutable(doc));
            case "episode": return toImmutable(OldEpisodeMapper.INSTANCE.toImmutable(doc));
            default: throw new RuntimeException("Unsupported document type : " + docType);
        }
    }

    @Deprecated
    default org.apache.lucene.document.Document toLucene(OldIndexDoc doc) {

        if (doc == null) return null;

        final Document lucene = new Document();

        Optional
            .ofNullable(doc.getDocType())
            .ifPresent(x -> lucene.add(new StringField(IndexField.DOC_TYPE, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getId())
            .ifPresent(x -> lucene.add(new StringField(IndexField.ID, x, Field.Store.YES)));
        /*
        Optional
            .ofNullable(doc.getExo())
            .ifPresent(x -> lucene.add(new StringField(IndexField.EXO, x, Field.Store.YES)));
            */
        Optional
            .ofNullable(doc.getTitle())
            .ifPresent(x -> lucene.add(new TextField(IndexField.TITLE, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getLink())
            .ifPresent(x -> lucene.add(new TextField(IndexField.LINK, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getDescription())
            .ifPresent(x -> lucene.add(new TextField(IndexField.DESCRIPTION, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getPodcastTitle())
            .ifPresent(x -> lucene.add(new TextField(IndexField.PODCAST_TITLE, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getPubDate())
            .map(DateMapper.INSTANCE::asString)
            .ifPresent(x -> lucene.add(new StringField(IndexField.PUB_DATE, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getImage())
            .ifPresent(x -> lucene.add(new TextField(IndexField.ITUNES_IMAGE, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getItunesAuthor())
            .ifPresent(x -> lucene.add(new TextField(IndexField.ITUNES_AUTHOR, x, Field.Store.NO)));
        Optional
            .ofNullable(doc.getItunesSummary())
            .ifPresent(x -> lucene.add(new TextField(IndexField.ITUNES_SUMMARY, x, Field.Store.YES)));
        Optional
            .ofNullable(doc.getChapterMarks())
            .ifPresent(x -> lucene.add(new TextField(IndexField.CHAPTER_MARKS, x, Field.Store.NO)));
        Optional
            .ofNullable(doc.getContentEncoded())
            .ifPresent(x -> lucene.add(new TextField(IndexField.CONTENT_ENCODED, x, Field.Store.NO)));
        Optional
            .ofNullable(doc.getTranscript())
            .ifPresent(x -> lucene.add(new TextField(IndexField.TRANSCRIPT, x, Field.Store.NO)));
        Optional
            .ofNullable(doc.getWebsiteData())
            .ifPresent(x -> lucene.add(new TextField(IndexField.WEBSITE_DATA, x, Field.Store.NO)));

        return lucene;
    }

    /*
    default SolrInputDocument toSolr(OldIndexDoc doc ) {

        if (doc == null) return null;

        final SolrInputDocument solr = new SolrInputDocument();

        Optional
            .ofNullable(doc.getDocType())
            .ifPresent(value -> solr.addField(IndexField.DOC_TYPE, value));
        Optional
            .ofNullable(doc.getExo())
            .ifPresent(value -> solr.addField(IndexField.EXO, value));
        Optional
            .ofNullable(doc.getTitle())
            .ifPresent(value -> solr.addField(IndexField.TITLE, value));
        Optional
            .ofNullable(doc.getLink())
            .ifPresent(value -> solr.addField(IndexField.LINK, value));
        Optional
            .ofNullable(doc.getDescription())
            .ifPresent(value -> solr.addField(IndexField.DESCRIPTION, value));
        Optional
            .ofNullable(doc.getPodcastTitle())
            .ifPresent(value -> solr.addField(IndexField.PODCAST_TITLE, value));
        Optional
            .ofNullable(doc.getPubDate())
            .map(DateMapper.INSTANCE::asString)
            .ifPresent(value -> solr.addField(IndexField.PUB_DATE, value));
        Optional
            .ofNullable(doc.getImage())
            .ifPresent(value -> solr.addField(IndexField.ITUNES_IMAGE, value));
        Optional
            .ofNullable(doc.getItunesAuthor())
            .ifPresent(value -> solr.addField(IndexField.ITUNES_AUTHOR, value));
        Optional
            .ofNullable(doc.getItunesSummary())
            .ifPresent(value -> solr.addField(IndexField.ITUNES_SUMMARY, value));
        Optional
            .ofNullable(doc.getChapterMarks())
            .ifPresent(value -> solr.addField(IndexField.CHAPTER_MARKS, value));
        Optional
            .ofNullable(doc.getContentEncoded())
            .ifPresent(value -> solr.addField(IndexField.CONTENT_ENCODED, value));
        Optional
            .ofNullable(doc.getTranscript())
            .ifPresent(value -> solr.addField(IndexField.TRANSCRIPT, value));
        Optional
            .ofNullable(doc.getWebsiteData())
            .ifPresent(value -> solr.addField(IndexField.WEBSITE_DATA, value));

        return solr;
    }
    */

}

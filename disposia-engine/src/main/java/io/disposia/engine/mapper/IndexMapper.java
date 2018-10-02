package io.disposia.engine.mapper;

import io.disposia.engine.domain.*;
import io.disposia.engine.domain.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

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
    ModifiableIndexDoc toModifiable(Podcast podcast);

    @Mapping(target = "docType", constant = "episode")
    @Mapping(source = "chapters", target = "chapterMarks")
    @Mapping(target = "itunesSummary", ignore = true)
    @Mapping(target = "transcript", ignore = true)
    @Mapping(target = "websiteData", ignore = true)
    ModifiableIndexDoc toModifiable(Episode episode);

    default ImmutableIndexDoc toImmutable(Podcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(this::toModifiable)
            .map(ModifiableIndexDoc::toImmutable)
            .orElse(null);
    }

    default ImmutableIndexDoc toImmutable(Episode episode) {
        return Optional
            .ofNullable(episode)
            .map(this::toModifiable)
            .map(ModifiableIndexDoc::toImmutable)
            .orElse(null);
    }

    default String map(List<Chapter> chapters){
        return Optional
            .ofNullable(chapters)
            .map(cs -> String.join("\n", cs.stream()
                .map(Chapter::getTitle)
                .collect(Collectors.toList())))
            .orElse(null);
    }

    default ModifiableIndexDoc toModifiable(IndexDoc doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> {
                if (doc instanceof ModifiableIndexDoc) {
                    return (ModifiableIndexDoc) d;
                } else {
                    return new ModifiableIndexDoc().from(d);
                }})
            .orElse(null);
    }

    default ImmutableIndexDoc toImmutable(IndexDoc doc) {
        return Optional
            .ofNullable(doc)
            .map(d -> {
                if (d instanceof ImmutableIndexDoc) {
                    return (ImmutableIndexDoc) d;
                } else {
                    return ((ModifiableIndexDoc) d).toImmutable();
                }})
            .orElse(null);
    }

    default ImmutableIndexDoc toImmutable(org.apache.lucene.document.Document doc) {

        if (doc == null) return null;

        final String docType = doc.get(IndexField.DOC_TYPE);
        if (isNullOrEmpty(docType)) {
            throw new RuntimeException("Document type is required but found NULL");
        }

        switch (docType) {
            case "podcast": return toImmutable(PodcastMapper.INSTANCE.toImmutable(doc));
            case "episode": return toImmutable(EpisodeMapper.INSTANCE.toImmutable(doc));
            default: throw new RuntimeException("Unsupported document type : " + docType);
        }
    }

    default ImmutableIndexDoc toImmutable(SolrDocument doc) {

        if (doc == null) return null;

        final String docType = (String) doc.getFieldValue(IndexField.DOC_TYPE);
        if (isNullOrEmpty(docType)) {
            throw new RuntimeException("Document type is required but found NULL");
        }

        switch (docType) {
            case "podcast": return toImmutable(PodcastMapper.INSTANCE.toImmutable(doc));
            case "episode": return toImmutable(EpisodeMapper.INSTANCE.toImmutable(doc));
            default: throw new RuntimeException("Unsupported document type : " + docType);
        }
    }

    default org.apache.lucene.document.Document toLucene(IndexDoc doc) {

        if (doc == null) return null;

        final Document lucene = new Document();

        Optional
            .ofNullable(doc.getDocType())
            .ifPresent(value -> lucene.add(new StringField(IndexField.DOC_TYPE, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getExo())
            .ifPresent(value -> lucene.add(new StringField(IndexField.EXO, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getTitle())
            .ifPresent(value -> lucene.add(new TextField(IndexField.TITLE, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getLink())
            .ifPresent(value -> lucene.add(new TextField(IndexField.LINK, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getDescription())
            .ifPresent(value -> lucene.add(new TextField(IndexField.DESCRIPTION, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getPodcastTitle())
            .ifPresent(value -> lucene.add(new TextField(IndexField.PODCAST_TITLE, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getPubDate())
            .map(DateMapper.INSTANCE::asString)
            .ifPresent(value -> lucene.add(new StringField(IndexField.PUB_DATE, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getImage())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_IMAGE, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getItunesAuthor())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_AUTHOR, value, Field.Store.NO)));
        Optional
            .ofNullable(doc.getItunesSummary())
            .ifPresent(value -> lucene.add(new TextField(IndexField.ITUNES_SUMMARY, value, Field.Store.YES)));
        Optional
            .ofNullable(doc.getChapterMarks())
            .ifPresent(value -> lucene.add(new TextField(IndexField.CHAPTER_MARKS, value, Field.Store.NO)));
        Optional
            .ofNullable(doc.getContentEncoded())
            .ifPresent(value -> lucene.add(new TextField(IndexField.CONTENT_ENCODED, value, Field.Store.NO)));
        Optional
            .ofNullable(doc.getTranscript())
            .ifPresent(value -> lucene.add(new TextField(IndexField.TRANSCRIPT, value, Field.Store.NO)));
        Optional
            .ofNullable(doc.getWebsiteData())
            .ifPresent(value -> lucene.add(new TextField(IndexField.WEBSITE_DATA, value, Field.Store.NO)));

        return lucene;
    }

    /*
    default SolrInputDocument toSolr(IndexDoc doc ) {

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

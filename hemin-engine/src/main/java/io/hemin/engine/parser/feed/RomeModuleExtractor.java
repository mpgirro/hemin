package io.hemin.engine.parser.feed;

import com.rometools.modules.atom.modules.AtomLinkModule;
import com.rometools.modules.content.ContentModule;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import io.hemin.engine.parser.feed.rome.PodloveSimpleChapterModule;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RomeModuleExtractor {

    public static Optional<FeedInformation> getItunesModule(SyndFeed feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> f.getModule(FeedInformation.URI))
            .map(f -> (FeedInformation) f);
    }

    public static List<Link> getAtomLinks(SyndFeed feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> f.getModule(AtomLinkModule.URI))
            .map(f -> (AtomLinkModule) f)
            .map(AtomLinkModule::getLinks)
            .orElse(Collections.emptyList());
    }

    public static List<Link> getAtomLinks(SyndEntry entry) {
        return Optional
            .ofNullable(entry)
            .map(f -> f.getModule(AtomLinkModule.URI))
            .map(f -> (AtomLinkModule) f)
            .map(AtomLinkModule::getLinks)
            .orElse(Collections.emptyList());
    }

    public static Optional<ContentModule> getContentModule(SyndEntry entry) {
        return Optional
            .ofNullable(entry)
            .map(e -> e.getModule(ContentModule.URI))
            .map(e -> (ContentModule) e);
    }

    public static Optional<EntryInformation> getItunesEntryInformation(SyndEntry entry) {
        return Optional
            .ofNullable(entry)
            .map(e -> e.getModule(EntryInformation.URI))
            .map(e -> (EntryInformation) e);
    }

    public static Optional<PodloveSimpleChapterModule> getPodloveSimpleChapterModule(SyndEntry entry) {
        return Optional
            .ofNullable(entry)
            .map(e -> e.getModule(PodloveSimpleChapterModule.URI))
            .map(e -> (PodloveSimpleChapterModule) e);
    }

}

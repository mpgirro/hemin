package io.disposia.engine.parse.rss;


import com.rometools.modules.atom.modules.AtomLinkModule;
import com.rometools.modules.content.ContentModule;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import io.disposia.engine.parse.rss.rome.PodloveSimpleChapterModule;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class RomeModuleExtractor {

    public static Optional<FeedInformation> getItunesModule(SyndFeed feed) {
        final Module itunesFeedModule = feed.getModule(FeedInformation.URI);
        final FeedInformation itunes = (FeedInformation) itunesFeedModule;
        return Optional.ofNullable(itunes);
    }

    public static List<Link> getAtomLinks(SyndFeed feed) {
        final Module atomFeedModule = feed.getModule(AtomLinkModule.URI);
        final AtomLinkModule atomLinkModule = (AtomLinkModule) atomFeedModule;
        return Optional
            .ofNullable(atomLinkModule)
            .map(AtomLinkModule::getLinks)
            .orElse(new LinkedList<>());
    }

    public static Optional<ContentModule> getContentModule(SyndEntry entry) {
        final Module contentModule = entry.getModule(ContentModule.URI);
        final ContentModule content = (ContentModule) contentModule;
        return Optional.ofNullable(content);
    }

    public static Optional<EntryInformation> getItunesEntryInformation(SyndEntry entry) {
        final Module itunesEntryModule = entry.getModule(EntryInformation.URI);
        final EntryInformation itunes = (EntryInformation) itunesEntryModule;
        return Optional.ofNullable(itunes);
    }

    public static Optional<PodloveSimpleChapterModule> getPodloveSimpleChapterModule(SyndEntry entry) {
        final Module pscEntryModule = entry.getModule(PodloveSimpleChapterModule.URI);
        final PodloveSimpleChapterModule simpleChapters = ((PodloveSimpleChapterModule) pscEntryModule);
        return Optional.ofNullable(simpleChapters);
    }

}

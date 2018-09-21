package exo.engine.parse.rss;

import com.rometools.modules.atom.modules.AtomLinkModule;
import com.rometools.modules.content.ContentModule;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.modules.itunes.types.Category;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import exo.engine.domain.dto.*;
import exo.engine.exception.FeedParsingException;
import exo.engine.parse.rss.rome.PodloveSimpleChapterModule;
import exo.engine.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Maximilian Irro
 */
public class RomeFeedParser implements FeedParser {

    private static final Logger log = LoggerFactory.getLogger(RomeFeedParser.class);

    private final Podcast thePodcast;
    private final List<Episode> theEpisodes;

    private RomeFeedParser(String xmlData) throws FeedParsingException {
        try {
            final InputSource inputSource = new InputSource( new StringReader( xmlData ) );
            final SyndFeedInput input = new SyndFeedInput();
            final SyndFeed feed = input.build(inputSource);

            this.thePodcast = parseFeed(feed);
            this.theEpisodes = extractEpisodes(feed);
        } catch (FeedException | IllegalArgumentException e) {
            throw new FeedParsingException("RomeFeedParser could not parse the feed", e);
        }
    }

    public static RomeFeedParser of(String xmlData) throws FeedParsingException {
        return new RomeFeedParser(xmlData);
    }

    @Override
    public Podcast getPodcast() {
        return this.thePodcast;
    }

    @Override
    public List<Episode> getEpisodes() {
        return this.theEpisodes;
    }

    private Podcast parseFeed(SyndFeed feed) {
        final ImmutablePodcast.Builder builder = ImmutablePodcast.builder();
        String link = UrlUtil.sanitize(feed.getLink());

        builder
            .setTitle(feed.getTitle())
            .setLink(link)
            .setDescription(feed.getDescription());

        SyndImage img = null;
        if (feed.getImage() != null) {
            img = feed.getImage();
            if(!isNullOrEmpty(img.getUrl())){
                builder.setImage(img.getUrl());
            }

            // now, it title/link/description were NULL, we use the values set in
            // the image tag as fallbacks because they usually have the same values
            if (isNullOrEmpty(feed.getTitle()) && !isNullOrEmpty(img.getTitle())) {
                builder.setTitle(img.getTitle());
            }
            if (link == null && !isNullOrEmpty(img.getLink())) {
                link = UrlUtil.sanitize(img.getLink());
                builder.setLink(link);
            }
            if (isNullOrEmpty(feed.getDescription()) && !isNullOrEmpty(img.getDescription())) {
                builder.setDescription(img.getDescription());
            }
        }

        if (feed.getPublishedDate() != null) {
            builder.setPubDate(LocalDateTime.ofInstant(feed.getPublishedDate().toInstant(), ZoneId.systemDefault()));
        }
        builder
            .setLanguage(feed.getLanguage())
            .setGenerator(feed.getGenerator())
            .setCopyright(feed.getCopyright())
            .setDocs(feed.getDocs())
            .setManagingEditor(feed.getManagingEditor());

        // access the <itunes:...> entries
        final Module itunesFeedModule = feed.getModule(FeedInformation.URI);
        final FeedInformation itunes = (FeedInformation) itunesFeedModule;
        if (itunes != null) {
            builder
                .setItunesSummary(itunes.getSummary())
                .setItunesAuthor(itunes.getAuthor())
                .setItunesKeywords(String.join(", ", itunes.getKeywords()));

            // we set the itunes image as a fallback only
            if (itunes.getImage() != null) {
                if (img == null || isNullOrEmpty(img.getUrl())) {
                    builder.setImage(itunes.getImage().toExternalForm());
                }
            }
            builder
                .setItunesCategories(new LinkedHashSet<>(
                    itunes.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toCollection(LinkedList::new))))
                .setItunesExplicit(itunes.getExplicit())
                .setItunesBlock(itunes.getBlock())
                .setItunesType(itunes.getType())
                .setItunesOwnerName(itunes.getOwnerName())
                .setItunesOwnerEmail(itunes.getOwnerEmailAddress());
            //builder.setItunesCategory(String.join(" | ", itunesFeedInfo.getCategories().stream().toImmutable(c->c.getName()).collect(Collectors.toCollection(LinkedList::new))));
        } else {
            log.debug("No iTunes Namespace elements found in Podcast");
        }

        // here I process the feed specific atom Links
        final List<Link> atomLinks = getAtomLinks(feed);
        for (Link atomLink : atomLinks) {
            if (atomLink.getRel().equals("http://podlove.org/deep-link")) {
                // TODO this should be a link to the episode website (but is it always though?!)
            } else if (atomLink.getRel().equals("payment")) {
                // TODO
            } else if (atomLink.getRel().equals("self")) {
                // TODO
            } else if (atomLink.getRel().equals("alternate")) {
                // TODO
            } else if (atomLink.getRel().equals("first")) {
                // TODO
            } else if (atomLink.getRel().equals("next")) {
                // TODO
            } else if (atomLink.getRel().equals("last")) {
                // TODO
            } else if (atomLink.getRel().equals("hub")) {
                // TODO
            } else if (atomLink.getRel().equals("search")) {
                // TODO
            } else if (atomLink.getRel().equals("via")) {
                // TODO
            } else if (atomLink.getRel().equals("related")) {
                // TODO
            } else if (atomLink.getRel().equals("prev-archive")) {
            } else {
                log.warn("Came across an <atom:link> with a relation I do not handle : '{}'", atomLink.getRel());
            }
        }

        return builder.create();
    }

    private List<Episode> extractEpisodes(SyndFeed feed) {
        final List<Episode> results = new LinkedList<>();
        for (SyndEntry e : feed.getEntries()) {
            final ImmutableEpisode.Builder builder = ImmutableEpisode.builder();

            builder
                .setTitle(e.getTitle())
                .setLink(UrlUtil.sanitize(e.getLink()));

            if (e.getPublishedDate() != null) {
                builder.setPubDate(LocalDateTime.ofInstant(e.getPublishedDate().toInstant(), ZoneId.systemDefault()));
            }
            //doc.setGuid(TODO);
            if (e.getDescription() != null) {
                builder.setDescription(e.getDescription().getValue());
            }

            if (e.getUri() != null) {
                builder.setGuid(e.getUri());
            }

            if (e.getEnclosures() != null && e.getEnclosures().size() > 0) {
                final SyndEnclosure enclosure = e.getEnclosures().get(0);
                builder
                    .setEnclosureUrl(enclosure.getUrl())
                    .setEnclosureType(enclosure.getType())
                    .setEnclosureLength(enclosure.getLength());
                if (e.getEnclosures().size() > 1) {
                    log.warn("Encountered multiple <enclosure> elements in <item> element");
                }
            }

            // access the <content:encoded> entries
            final Module contentModule = e.getModule(ContentModule.URI);
            final ContentModule content = (ContentModule) contentModule;
            if (content != null) {
                if (content.getEncodeds().size() > 0) {
                    builder.setContentEncoded(content.getEncodeds().get(0));
                    if (content.getEncodeds().size() > 1) {
                        log.warn("Encountered multiple <content:encoded> elements in <item> element");
                    }
                }
            }

            // access the <itunes:...> entries
            final Module itunesEntryModule = e.getModule(EntryInformation.URI);
            final EntryInformation itunes = (EntryInformation) itunesEntryModule;
            if (itunes != null) {
                if (itunes.getImage() != null) {
                    builder.setImage(itunes.getImage().toExternalForm());
                }
                if (itunes.getDuration() != null) {
                    builder.setItunesDuration(itunes.getDuration().toString());
                }
                builder
                    .setItunesSubtitle(itunes.getSubtitle())
                    .setItunesAuthor(itunes.getAuthor())
                    .setItunesSummary(itunes.getSummary())
                    .setItunesSeason(itunes.getSeason())
                    .setItunesEpisode(itunes.getEpisode())
                    .setItunesEpisodeType(itunes.getEpisodeType());
            } else {
                log.debug("No iTunes Namespace elements found in Episode");
            }

            // access the <psc:chapter> entries
            final Module pscEntryModule = e.getModule(PodloveSimpleChapterModule.URI);
            final PodloveSimpleChapterModule simpleChapters = ((PodloveSimpleChapterModule) pscEntryModule);
            if (simpleChapters != null) {
                if (simpleChapters.getChapters() != null && simpleChapters.getChapters().size() > 0) {
                    builder.setChapters(
                        simpleChapters.getChapters().stream()
                            .map(sc -> ImmutableChapter.builder()
                                .setStart(sc.getStart())
                                .setTitle(sc.getTitle())
                                .setHref(sc.getHref())
                                .create())
                            .collect(Collectors.toList()));
                }
            } else {
                log.debug("No Podlove Simple Chapter marks found in Episode");
            }

            results.add(builder.create());
        }

        return results;
    }

    private List<Link> getAtomLinks(SyndFeed syndFeed) {
        final Module atomFeedModule = syndFeed.getModule(AtomLinkModule.URI);
        final AtomLinkModule atomLinkModule = (AtomLinkModule) atomFeedModule;
        return Optional
            .ofNullable(atomLinkModule)
            .map(AtomLinkModule::getLinks)
            .orElse(new LinkedList<>());
    }

}


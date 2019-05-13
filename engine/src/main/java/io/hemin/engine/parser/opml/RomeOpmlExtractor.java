package io.hemin.engine.parser.opml;

import com.rometools.rome.feed.WireFeed;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.opml.feed.opml.Opml;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RomeOpmlExtractor {

    public static List<Outline> getOutlines(WireFeed feed) {
        return Optional
            .ofNullable(feed)
            .map(f -> (Opml) f)
            .map(Opml::getOutlines)
            .orElse(Collections.emptyList());
    }

}

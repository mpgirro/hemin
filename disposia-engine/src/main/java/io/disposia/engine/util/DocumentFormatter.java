package io.disposia.engine.util;

import io.disposia.engine.olddomain.OldIndexDoc;
import org.jsoup.Jsoup;

public class DocumentFormatter {

    private static final String NEWLINE = System.getProperty("line.separator");

    public static String cliFormat(OldIndexDoc doc){
        final StringBuilder builder = new StringBuilder();
        switch (doc.getDocType()) {
            case "podcast":
                builder
                    .append(doc.getTitle())
                    .append(NEWLINE)
                    .append("[OldPodcast] ")
                    .append(NEWLINE);
                if (doc.getPubDate() != null) builder.append(doc.getPubDate());
                builder
                    .append(NEWLINE)
                    .append(Jsoup.parse(doc.getDescription()).text())
                    .append(NEWLINE)
                    .append(doc.getLink())
                    .append(NEWLINE);
                break;
            case "episode":
                builder
                    .append(doc.getTitle())
                    .append(NEWLINE)
                    .append(doc.getPodcastTitle())
                    .append(NEWLINE)
                    .append("[OldEpisode] ");
                if (doc.getPubDate() != null) builder.append(doc.getPubDate());
                builder
                    .append(NEWLINE)
                    .append(Jsoup.parse(doc.getDescription()).text())
                    .append(NEWLINE)
                    .append(doc.getLink())
                    .append(NEWLINE);
                break;
            default:
                throw new RuntimeException("Forgot to support new type : " + doc.getDocType());
        }
        return builder.toString();
    }
}

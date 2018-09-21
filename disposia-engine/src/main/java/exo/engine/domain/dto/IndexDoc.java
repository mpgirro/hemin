package exo.engine.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
@Value.Immutable
@Value.Modifiable                   // generates implementation with setters, required by mappers
@Value.Style(
        jdkOnly    = true,              // prevent usage of Guava collections
        get        = {"is*", "get*"},   // Detect 'get' and 'is' prefixes in accessor methods
        init       = "set*",
        create     = "new",             // generates public no args constructor
        build      = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableIndexDoc.class)
@JsonDeserialize(as = ImmutableIndexDoc.class)
public interface IndexDoc {

    @Nullable
    String getDocType();

    @Nullable
    String getExo();

    @Nullable
    String getTitle();

    @Nullable
    String getLink();

    @Nullable
    String getDescription();

    @Nullable
    LocalDateTime getPubDate();

    @Nullable
    String getImage();

    @Nullable
    String getItunesAuthor();

    @Nullable
    String getItunesSummary();

    @Nullable
    String getPodcastTitle(); // will be the same as the title if marshalled from a Podcast

    @Nullable
    String getChapterMarks();

    @Nullable
    String getContentEncoded();

    @Nullable
    String getTranscript();

    @Nullable
    String getWebsiteData();

}

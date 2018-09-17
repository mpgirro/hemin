package exo.engine.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * This class represents an toImmutable Data Transfer Object.
 * The implementation is generated using https://immutables.github.io
 * Instances should be created using the builder pattern (required methods will be
 * generated), but to be able to generate MapStruct mapper methods, also setter will
 * be included in the generated implementation.
 *
 * From this class two implementations will be generated:
 *  - ImmutablePodcast
 *  - ModifiablePodcast
 *
 * <s>Note</s>: This class is abstract instead of an interface to be able to overwrite
 * the java.lang.Object equals(), hashCode() and toString() methods, because Java 8
 * default interface methods can never overwrite class implementations
 *
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
@JsonSerialize(as = ImmutablePodcastDTO.class)
@JsonDeserialize(as = ImmutablePodcastDTO.class)
public interface PodcastDTO {

    @Nullable
    Long getId();

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
    LocalDateTime getLastBuildDate();

    @Nullable
    String getLanguage();

    @Nullable
    String getGenerator();

    @Nullable
    String getCopyright();

    @Nullable
    String getDocs();

    @Nullable
    String getManagingEditor();

    @Nullable
    String getImage();

    @Nullable
    String getItunesSummary();

    @Nullable
    String getItunesAuthor();

    @Nullable
    String getItunesKeywords();

    @Nullable
    Set<String> getItunesCategories();

    @Nullable
    Boolean getItunesExplicit();

    @Nullable
    Boolean getItunesBlock();

    @Nullable
    String getItunesType();

    @Nullable
    String getItunesOwnerName();

    @Nullable
    String getItunesOwnerEmail();

    @Nullable
    String getFeedpressLocale();

    @Nullable
    String getFyydVerify();

    @Nullable
    Integer getEpisodeCount();

    @Nullable
    LocalDateTime getRegistrationTimestamp();

    @Nullable
    Boolean getRegistrationComplete();

}

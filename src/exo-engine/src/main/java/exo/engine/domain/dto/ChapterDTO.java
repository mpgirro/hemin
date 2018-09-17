package exo.engine.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;

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
@JsonSerialize(as = ImmutableChapterDTO.class)
@JsonDeserialize(as = ImmutableChapterDTO.class)
public interface ChapterDTO {

    @Nullable
    Long getId();

    @Nullable
    Long getEpisodeId();

    @Nullable
    String getStart();

    @Nullable
    String getTitle();

    @Nullable
    String getHref();

    @Nullable
    String getImage();

    @Nullable
    String getEpisodeExo();

}
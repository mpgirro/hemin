package io.disposia.engine.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.disposia.engine.domain.FeedStatus;
import io.disposia.engine.domain.ImmutableFeed;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Value.Immutable
@Value.Modifiable                   // generates implementation with setters, required by mappers
@Value.Style(
    jdkOnly = true,              // prevent usage of Guava collections
    get     = {"is*", "get*"},   // Detect 'get' and 'is' prefixes in accessor methods
    init    = "set*",
    create  = "new",             // generates public no args constructor
    build   = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableFeed.class)
@JsonDeserialize(as = ImmutableFeed.class)
public interface Feed {

    @Nullable
    String getId();

    @Nullable
    String getPodcastId();

    /*
    @Nullable
    String getExo();

    @Nullable
    String getPodcastExo();
    */

    @Nullable
    String getUrl();

    @Nullable
    LocalDateTime getLastChecked();

    @Nullable
    FeedStatus getLastStatus();

    @Nullable
    LocalDateTime getRegistrationTimestamp();

}

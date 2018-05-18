package echo.core.async.parser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author Maximilian Irro
 */
@Value.Immutable
@Value.Style(
    jdkOnly    = true,              // prevent usage of Guava collections
    get        = {"is*", "get*"},   // Detect 'get' and 'is' prefixes in accessor methods
    init       = "set*",
    create     = "new",             // generates public no args constructor
    defaults   = @Value.Immutable(builder = false),  // We may also disable builder
    build      = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableUpdateFeedParserJob.class)
@JsonDeserialize(as = ImmutableUpdateFeedParserJob.class)
public interface UpdateFeedParserJob extends ParserJob {

    @Value.Parameter
    String getExo();

    @Value.Parameter
    String getUrl();

    @Value.Parameter
    String getData();

}

package echo.core.async.index;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import echo.core.domain.dto.IndexDocDTO;
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
@JsonSerialize(as = ImmutableAddOrUpdateDocIndexJob.class)
@JsonDeserialize(as = ImmutableAddOrUpdateDocIndexJob.class)
public interface AddOrUpdateDocIndexJob extends IndexJob {

    @Value.Parameter
    IndexDocDTO getIndexDoc();

}

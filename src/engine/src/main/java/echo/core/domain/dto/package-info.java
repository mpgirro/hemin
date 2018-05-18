/**
 * Immutable versions of the Data Transfer Objects. Required if data is
 * not transmitted via plaintext serialization (e.g. JSON/XML), but for
 * instance via Akka, which does passing values by reference if message
 * is send within local scope.
 *
 * @author Maximilian Irro
 */
package echo.core.domain.dto;

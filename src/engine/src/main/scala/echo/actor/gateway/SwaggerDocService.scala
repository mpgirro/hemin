package echo.actor.gateway

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import echo.actor.gateway.service.{EpisodeGatewayService, PodcastGatewayService, SearchGatewayService}
import io.swagger.models.ExternalDocs
import io.swagger.models.auth.BasicAuthDefinition

/**
  * see : https://github.com/pjfanning/swagger-akka-http-sample/blob/master/src/main/scala/com/example/akka/swagger/SwaggerDocService.scala
  *
  *
  * @author Maximilian Irro
  */
object SwaggerDocService extends SwaggerHttpService {
    override val apiClasses = Set(classOf[SearchGatewayService],classOf[PodcastGatewayService],classOf[EpisodeGatewayService])
    override val host = "localhost:3030" // TODO replace by config values
    override val info = Info(version = "1.0")
    // override val externalDocs = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
    override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
    //override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}

package hemin.api

import com.iheart.playSwagger.SwaggerSpecGenerator
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.JsString
import play.api.mvc._

class SwaggerSpecs @Inject() (cc: ControllerComponents,
                              config: Configuration)
  extends AbstractController(cc) {

  implicit val cl: ClassLoader = getClass.getClassLoader

  lazy val generator = SwaggerSpecGenerator(false, "hemin.api.v1.rest", "hemin.engine.model")

  // Get's host configuration.
  val host: String = config.get[String]("swagger.api.basepath")

  lazy val swagger = Action { request =>
    generator.generate().map(_ + ("host" -> JsString(host))).fold(
      e => InternalServerError("Couldn't generate swagger."),
      s => Ok(s))
  }

  def specs = swagger
}

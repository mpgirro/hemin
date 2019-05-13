import com.google.inject.AbstractModule
import io.hemin.api.v1.service.EngineService
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule with ScalaModule {

    override def configure(): Unit = {
        /*
         * This is important!
         * We make an eager binding of the EngineService.
         * Otherwise the component is lazily instantiated
         * and this will make everything super slow
         */
        bind(classOf[EngineService]).asEagerSingleton()
    }
}

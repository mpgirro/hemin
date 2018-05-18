package echo.actor.directory.service

import javax.persistence.EntityManager

/**
  * @author Maximilian Irro
  */
trait DirectoryService {

    /*
    protected val rfb: RepositoryFactoryBuilder
    protected var repositoryFactory: JpaRepositoryFactory = _
    protected var repository: T = _
    */

    def refresh(em: EntityManager): Unit

}

package hemin.engine.graph.repository

import org.neo4j.driver.v1.{Driver, Session}

trait GraphRepository {

  protected[this] val driver: Driver

  protected[this] lazy val session: Session = driver.session

}

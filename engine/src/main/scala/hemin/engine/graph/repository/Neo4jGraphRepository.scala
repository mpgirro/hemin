package hemin.engine.graph.repository

import org.neo4j.driver.v1.Driver

class Neo4jGraphRepository (neo4jDriver: Driver)
  extends GraphRepository {

  override protected[this] val driver: Driver = neo4jDriver

}

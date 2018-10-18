package io.hemin.engine.catalog

object CatalogConfig {
  val dispatcherId: String = "hemin.catalog.dispatcher"
}

/**
  * Configuration for [[io.hemin.engine.catalog.CatalogStore]]
  */
final case class CatalogConfig (
  dispatcherId: String = CatalogConfig.dispatcherId,
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
)

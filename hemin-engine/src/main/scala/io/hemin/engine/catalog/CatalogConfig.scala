package io.hemin.engine.catalog

object CatalogConfig {
  val dispatcher: String = "hemin.catalog.dispatcher"
}

/** Configuration for [[io.hemin.engine.catalog.CatalogStore]] */
final case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
) {
  val dispatcher: String = CatalogConfig.dispatcher
  val mailbox: String = CatalogPriorityMailbox.name
}

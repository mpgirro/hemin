package io.hemin.engine.catalog

/**
  * Configuration for [[io.hemin.engine.catalog.CatalogStore]]
  */
final case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
)

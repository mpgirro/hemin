package io.hemin.engine.catalog

/**
  * Configuration for [[io.hemin.engine.catalog.CatalogStore]]
  */
case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
)

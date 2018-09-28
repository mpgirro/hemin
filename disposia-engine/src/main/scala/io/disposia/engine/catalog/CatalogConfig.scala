package io.disposia.engine.catalog

/**
  * Configuration for [[io.disposia.engine.catalog.CatalogStore]]
  */
case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
)

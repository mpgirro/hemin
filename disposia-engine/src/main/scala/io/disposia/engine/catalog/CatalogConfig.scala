package io.disposia.engine.catalog

/**
  * Configuration for [[io.disposia.engine.catalog.CatalogStore]]
  */
case class CatalogConfig (
  workerCount: Int,
  databaseUrl: String,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
)

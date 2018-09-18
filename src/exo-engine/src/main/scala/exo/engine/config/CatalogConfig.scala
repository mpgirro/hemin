package exo.engine.config

/**
  * Configuration for [[exo.engine.catalog.CatalogStore]]
  */
case class CatalogConfig (
    workerCount: Int,
    databaseUrl: String,
    maxPageSize: Int
)

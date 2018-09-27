package io.disposia.engine.catalog.service

import javax.persistence.EntityManager

@Deprecated
trait CatalogService {

    def refresh(em: EntityManager): Unit

}

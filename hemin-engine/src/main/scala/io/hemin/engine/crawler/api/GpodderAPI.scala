package io.hemin.engine.crawler.api

class GpodderAPI extends ExternalDirectoryAPI {

  override def baseUrl: String = throw new UnsupportedOperationException("GpodderAPI.baseUrl not yet implemented")

  override def getFeedUrls(count: Int): List[String] = throw new UnsupportedOperationException("GpodderAPI.getFeedUrls not yet implemented")

}

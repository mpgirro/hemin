package io.hemin.engine.crawler.api

class FyydAPI extends ExternalDirectoryAPI {

  override def baseUrl: String = throw new UnsupportedOperationException("FyydAPI.baseUrl not yet implemented")

  override def getFeedUrls(count: Int): List[String] = throw new UnsupportedOperationException("FyydAPI.getFeedUrls not yet implemented")

}

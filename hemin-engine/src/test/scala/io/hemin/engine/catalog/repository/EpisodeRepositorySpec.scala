package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.TestContext
import io.hemin.engine.model.Episode
import io.hemin.engine.util.TimeUtil
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers}

class EpisodeRepositorySpec
  extends AsyncFlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter {

  private val log = Logger(getClass)

  var testContext: TestContext = _
  var episodeRepository: EpisodeRepository = _

  before {
    testContext = new TestContext // also starts the embedded MongoDB
    episodeRepository = testContext.repositoryFactory.getEpisodeRepository
  }

  after {
    //mongoStop(mongoProps)
    testContext.stop() // stops the embedded MongoDB
  }

  "The EpisodeRepository" should "not eventually retrieve an episode with a future pubDate" in {

    val now = TimeUtil.now - 3600L // reduce now a bit, just to avoid race conditions
    val tomorrow = now + 86400000L

    val episode1 = Episode(
      id = Some("id1"),
      title = Some("test episode 1"),
      pubDate = Some(now)
    )

    val episode2 = Episode(
      id = Some("id2"),
      title = Some("test episode 2"),
      pubDate = Some(tomorrow)
    )

    // Note: we are aware of the sequencing that happens here, and we embrace it
    val results = for {
      e1 <- episodeRepository.save(episode1)
      e2 <- episodeRepository.save(episode2)
      r <-  episodeRepository.findAll(1, 2)
    } yield r

    results.map { r =>
        assert(r.exists(_.id.contains("id1")), "An episode with a valid pubDate was not retrieved")
        assert(!r.exists(_.id.contains("id2")), "An episode with an invalid pubDate (in the future) was retrieved")
      }

  }

}

package hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import hemin.engine.MongoTestContext
import hemin.engine.model.Episode
import hemin.engine.util.TimeUtil
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, BeforeAndAfterAll, Matchers}

class EpisodeRepositorySpec
  extends AsyncFlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with BeforeAndAfterAll {

  private val log = Logger(getClass)

  val testId1 = "id1"
  val testId2 = "id2"

  val testContext = new MongoTestContext // also starts the embedded MongoDB

  var episodeRepository: EpisodeRepository = _

  before {
    //testContext = new MongoTestContext // also starts the embedded MongoDB
    episodeRepository = testContext.repositoryFactory.getEpisodeRepository
  }

  after {
    testContext.repositoryFactory.dropAll()
  }

  override def afterAll: Unit = {
    testContext.stop() // stops the embedded MongoDB
  }

  "The EpisodeRepository" should "not eventually retrieve an episode with a future pubDate" in {

    val now = TimeUtil.now - 3600L // reduce now a bit, just to avoid race conditions
    val tomorrow = now + 86400000L

    val episode1 = Episode(
      id = Some(testId1),
      title = Some("test episode 1"),
      pubDate = Some(now)
    )

    val episode2 = Episode(
      id = Some(testId2),
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
        assert(r.exists(_.id.contains(testId1)), "An episode with a valid pubDate was not retrieved")
        assert(!r.exists(_.id.contains(testId2)), "An episode with an invalid pubDate (in the future) was retrieved")
      }

  }

}

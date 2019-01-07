package io.hemin.engine.catalog.repository

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import io.hemin.engine.TestContext
import io.hemin.engine.model.Episode
import io.hemin.engine.util.TimeUtil
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class EpisodeRepositorySpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with MongoEmbedDatabase
    with BeforeAndAfter {

  var mongoProps: MongodProps = _
  var testContext: TestContext = _
  var episodeRepository: EpisodeRepository = _

  before {
    mongoProps = mongoStart()
    testContext = new TestContext(mongoProps)
    episodeRepository = testContext.repositoryFactory.getEpisodeRepository
  }

  after {
    mongoStop(mongoProps)
  }

  "The EpisodeRepository" should "not retrieve episodes with a future pubDate" in {

    val now = TimeUtil.now()
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

    episodeRepository.save(episode1)
    episodeRepository.save(episode2)

    // TODO wait unit save finished

    // TODO query database and check that episodes is not in the results

    assert(true) // TODO

  }

}

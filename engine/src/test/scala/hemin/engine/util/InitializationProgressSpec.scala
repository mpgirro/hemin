package hemin.engine.util

import org.scalatest.{FlatSpec, Matchers}

class InitializationProgressSpec
  extends FlatSpec
    with Matchers {

  val subsys1: String = "subsys1"
  val subsys2: String = "subsys2"
  val subsys3: String = "subsys3"

  "The InitializationProgress" should "signal completion once all subsystems reported in" in {
    val progress: InitializationProgress = new InitializationProgress(Set(subsys1, subsys2, subsys3))

    progress.signalCompletion(subsys1)
    progress.signalCompletion(subsys2)
    progress.signalCompletion(subsys3)
    progress.isFinished shouldBe true
  }

  "The InitializationProgress" should "not signal completion until all subsystems reported in" in {
    val progress: InitializationProgress = new InitializationProgress(Set(subsys1, subsys2, subsys3))

    progress.signalCompletion(subsys1)
    assert(!progress.isFinished, "The InitializationProgress signaled completion although 2 expected subsystems are still missing")

    progress.signalCompletion(subsys2)
    assert(!progress.isFinished, "The InitializationProgress signaled completion although 1 expected subsystem is still missing")

    progress.signalCompletion(subsys3)
    assert(progress.isFinished, "The InitializationProgress does not signal completion although all expected subsystems reported their completion")
  }

}

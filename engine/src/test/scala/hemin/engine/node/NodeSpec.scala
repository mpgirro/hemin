package hemin.engine.node

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.HeminEngine
import org.scalatest.Matchers

class NodeSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with Matchers {

}

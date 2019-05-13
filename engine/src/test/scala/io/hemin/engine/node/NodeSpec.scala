package io.hemin.engine.node

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import io.hemin.engine.HeminEngine
import org.scalatest.Matchers

class NodeSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with Matchers {

}

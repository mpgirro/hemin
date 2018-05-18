package echo.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ClusterEvent._

/**
  * @author Maximilian Irro
  */

object ClusterDomainEventListener {
    def name: String = "cluster-domain-listener"
    def props(): Props = Props(new ClusterDomainEventListener())
}

class ClusterDomainEventListener extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])

    override def receive: Receive = {
        case MemberUp(member) => log.info(s"$member UP.")
        case MemberExited(member) => log.info(s"$member EXITED.")
        case MemberRemoved(m, previousState) =>
            if(previousState == MemberStatus.Exiting) {
                log.info(s"Member $m gracefully exited, REMOVED.")
            } else {
                log.info(s"$m downed after unreachable, REMOVED.")
            }
        case UnreachableMember(m) => log.info(s"$m UNREACHABLE")
        case ReachableMember(m) => log.info(s"$m REACHABLE")
        case s: CurrentClusterState => log.info(s"cluster state: $s")
    }

    override def postStop(): Unit = {
        Cluster(context.system).unsubscribe(self)
        super.postStop()
    }

}

package com.github.betavojnik

import com.github.betavojnik.actors.CoordinatorActor
import org.apache.pekko.actor.AddressFromURIString
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem}
import org.apache.pekko.cluster.typed.{Cluster, Join, JoinSeedNodes}

object Main {
  private val system  = ActorSystem(CoordinatorActor(), "FederatedLearningNode")
  private val cluster = Cluster(system)

  def main(args: Array[String]): Unit = {
    val seedNodes = args.toList.map(AddressFromURIString.parse)

    if (seedNodes.nonEmpty) {
      println("TRAAAALAAAA")
      cluster.manager ! JoinSeedNodes(seedNodes)
    } else {
      cluster.manager ! Join(cluster.selfMember.address)
    }
  }
}

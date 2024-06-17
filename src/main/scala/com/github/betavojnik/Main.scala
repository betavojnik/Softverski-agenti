package com.github.betavojnik

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "FederatedLearning")
  }
}
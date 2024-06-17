package com.github.betavojnik

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object Main {
  def main(args: Array[String]): Unit =
    ActorSystem(Behaviors.empty, "FederatedLearning")
}

package com.github.betavojnik.actors

import com.github.betavojnik.services.TrainerService
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object TrainerActor {
  def apply(coordinatorActorRef: ActorRef[CoordinatorActor.Data]): Behavior[_] = {
    val service = new TrainerService()

    val model: Unit = service.train()

    coordinatorActorRef ! CoordinatorActor.LocalModelFromTrainer(model)

    Behaviors.same
  }
}

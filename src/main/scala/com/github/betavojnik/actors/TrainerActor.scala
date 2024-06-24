package com.github.betavojnik.actors

import com.github.betavojnik.actors.CoordinatorActor.listening
import com.github.betavojnik.services.TrainerService
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object TrainerActor {
  sealed trait Data
  final case class AggregateLocalModels() extends Data
  private final case class GlobalModel() extends Data

  def apply(coordinatorActorRef: ActorRef[CoordinatorActor.Data]): Behavior[Data] =
    Behaviors.setup { _ =>
      val service = new TrainerService()

      val model: List[List[Double]] = service.train()
      coordinatorActorRef ! CoordinatorActor.LocalModelFromTrainer(model)

      listening(service, coordinatorActorRef)

    }

  def listening(service: TrainerService, coordinatorActorRef: ActorRef[CoordinatorActor.Data]): Behavior[Data] =
    Behaviors.receive { (ctx, message) =>
      message match {
        case AggregateLocalModels() =>
          ctx.self ! GlobalModel()

          Behaviors.same
        case GlobalModel() =>
          val model: Unit = service.train()

          coordinatorActorRef ! CoordinatorActor.LocalModelFromTrainer(model)

          Behaviors.same
      }
    }
}

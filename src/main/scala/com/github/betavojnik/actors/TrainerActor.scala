package com.github.betavojnik.actors

import com.github.betavojnik.models.ModelData
import com.github.betavojnik.services.{AverageAggregatorService, TrainerService}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}

object TrainerActor {
  sealed trait Data
  final case class AggregateLocalModelData(data: List[ModelData]) extends Data
  private final case class GlobalModelData(data: ModelData)       extends Data

  def apply(coordinatorActorRef: ActorRef[CoordinatorActor.Data]): Behavior[Data] =
    Behaviors.setup { ctx =>
      val service = new TrainerService(ctx.log)

      val model: ModelData = service.train(None)
      coordinatorActorRef ! CoordinatorActor.LocalModelDataFromTrainer(model)

      listening(service, coordinatorActorRef)
    }

  def listening(service: TrainerService, coordinatorActorRef: ActorRef[CoordinatorActor.Data]): Behavior[Data] =
    Behaviors.receive { (ctx, message) =>
      message match {
        case AggregateLocalModelData(data) =>
          val globalData = AverageAggregatorService.aggregate(data)

          ctx.self ! GlobalModelData(globalData)

          Behaviors.same
        case GlobalModelData(data) =>
          val model: ModelData = service.train(Some(data))

          coordinatorActorRef ! CoordinatorActor.LocalModelDataFromTrainer(model)

          Behaviors.same
      }
    }
}

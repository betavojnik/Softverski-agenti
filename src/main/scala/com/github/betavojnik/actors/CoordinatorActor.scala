package com.github.betavojnik.actors

import com.github.betavojnik.models.ModelData
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object CoordinatorActor {
  sealed trait Data
  final case class LocalModelDataFromTrainer(data: ModelData)             extends Data
  private final case class LocalModelDataFromCoordinator(data: ModelData) extends Data

  /**
   * Creates an instance of a coordinator actor for exchanging local models with other coordinators
   */
  def apply(): Behavior[Data] =
    Behaviors.setup { ctx =>
      ctx.spawn(TrainerActor(ctx.self), "TrainerActor")

      listening()
    }

  def listening(): Behavior[Data] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case LocalModelDataFromCoordinator(_) =>
          Behaviors.same
        case LocalModelDataFromTrainer(_) =>
          Behaviors.same
        case _ =>
          ctx.log.debug("Message received")

          Behaviors.same
      }
    }
}

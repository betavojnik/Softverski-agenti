package com.github.betavojnik.actors

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object CoordinatorActor {
  sealed trait Data
  final case class LocalModelFromTrainer(model: Any)             extends Data
  private final case class LocalModelFromCoordinator(model: Any) extends Data

  /**
   * Creates an instance of a coordinator actor for exchanging local models with other coordinators
   */
  def apply(): Behavior[Data] =
    Behaviors.setup { ctx =>
      ctx.spawn(TrainerActor(ctx.self), "Training")
      listening()
    }

  def listening(): Behavior[Data] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case LocalModelFromCoordinator(_) =>
          Behaviors.same
        case LocalModelFromTrainer(_) =>
          Behaviors.same
        case _ =>
          ctx.log.debug("Message received")

          Behaviors.same
      }
    }
}

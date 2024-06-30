package com.github.betavojnik.actors

import com.github.betavojnik.models.ModelData
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.cluster.MemberStatus
import org.apache.pekko.cluster.ddata.typed.scaladsl.Replicator.{Get, GetResponse, Update}
import org.apache.pekko.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import org.apache.pekko.cluster.ddata.{LWWRegister, LWWRegisterKey, SelfUniqueAddress}
import org.apache.pekko.cluster.typed.Cluster

import scala.concurrent.duration.DurationInt

object CoordinatorActor {
  sealed trait Data
  final case class LocalModelDataFromTrainer(data: ModelData)                     extends Data
  private final case class LocalModelDataFromCoordinator(data: Option[ModelData]) extends Data
  private case object UpdatedSelfLocalModel                                       extends Data

  /**
   * Creates an instance of a coordinator actor for exchanging local models with other coordinators
   */
  def apply(): Behavior[Data] =
    Behaviors.setup { ctx =>
      val trainerActor = ctx.spawn(TrainerActor(ctx.self), "TrainerActor")

      training(trainerActor)
    }

  def listening(
    trainerActor: ActorRef[TrainerActor.Data],
    waitingCount: Int,
    modelData: List[ModelData] = Nil
  ): Behavior[Data] =
    Behaviors.receive { (_, msg) =>
      msg match {
        case LocalModelDataFromCoordinator(data) =>
          val newModelData    = data.fold(modelData)(modelData :+ _)
          val newWaitingCount = waitingCount - 1

          if (newWaitingCount == 0) {
            trainerActor ! TrainerActor.AggregateLocalModelData(newModelData)

            training(trainerActor)
          } else {
            listening(trainerActor, newWaitingCount, newModelData)
          }
        case _ =>
          Behaviors.same
      }
    }

  private def training(trainerActor: ActorRef[TrainerActor.Data]): Behavior[Data] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case LocalModelDataFromTrainer(data) =>
          implicit val node: SelfUniqueAddress = DistributedData(ctx.system).selfUniqueAddress

          val replicator = DistributedData(ctx.system).replicator

          val register = LWWRegister.create(data)
          val key      = LWWRegisterKey[ModelData](node.uniqueAddress.toString)
          replicator ! Update(key, register, Replicator.WriteAll(10.seconds)) { register =>
            register.withValue(node, data)
          }(ctx.messageAdapter(_ => UpdatedSelfLocalModel))

          Behaviors.same
        case UpdatedSelfLocalModel =>
          val replicator = DistributedData(ctx.system).replicator

          val registerKeyList = registerKeys(Cluster(ctx.system))

          registerKeyList.foreach { key =>
            replicator ! Get(
              key,
              Replicator.ReadLocal,
              ctx.messageAdapter[GetResponse[LWWRegister[ModelData]]] {
                case rsp @ Replicator.GetSuccess(key) =>
                  LocalModelDataFromCoordinator(Some(rsp.get(key).value))
                case _ =>
                  LocalModelDataFromCoordinator(None)
              }
            )
          }

          listening(trainerActor, registerKeyList.size)
        case _ =>
          Behaviors.same
      }
    }

  private def registerKeys(cluster: Cluster): List[LWWRegisterKey[ModelData]] =
    cluster.state.members
      .filter(_.status == MemberStatus.Up)
      .map(_.uniqueAddress.toString)
      .toList
      .map(LWWRegisterKey(_))
}

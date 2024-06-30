package com.github.betavojnik.services

import com.github.betavojnik.Matrix2D
import com.github.betavojnik.models.ModelData

object AverageAggregatorService {
  def aggregate(modelData: List[ModelData]): ModelData = {
    val averagedWeights = averageWeights(modelData.map(_.weights))
    val averagedBiases  = averageBiases(modelData.map(_.biases))

    ModelData(averagedWeights, averagedBiases)
  }

  private def averageWeights(weightsList: List[List[Matrix2D[Double]]]): List[Matrix2D[Double]] =
    weightsList.foldLeft(createAccumulatorForWeights(weightsList.head)) {
      _.zip(_).map { case (acc, mat) =>
        acc.zip(mat).map { case (row1, row2) =>
          row1.zip(row2).map { case (el1, el2) => el1 + el2 / weightsList.size }
        }
      }
    }

  private def averageBiases(biasesList: List[List[List[Double]]]): List[List[Double]] =
    biasesList.foldLeft(createAccumulatorForBiases(biasesList.head)) {
      _.zip(_).map { case (row1, row2) =>
        row1.zip(row2).map { case (el1, el2) => el1 + el2 / biasesList.size }
      }
    }

  private def createAccumulatorForWeights(weights: List[Matrix2D[Double]]): List[Matrix2D[Double]] =
    weights.map(_.map(_.map(_ => 0.0)))

  private def createAccumulatorForBiases(biases: List[List[Double]]): List[List[Double]] =
    biases.map(_.map(_ => 0.0))
}

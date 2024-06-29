package com.github.betavojnik.services

import com.github.betavojnik.models.ModelData

object AggregatorService {
  private type Matrix[T] = List[List[T]]

  def aggregate(modelData: List[ModelData]): ModelData = {
    val averagedWeights = averageMatrices(modelData.map(_.weights))
    val averagedBiases  = averageMatrices(modelData.map(_.biases))

    ModelData(averagedWeights, averagedBiases)
  }

  private def averageMatrices(matrices: List[Matrix[Double]]): Matrix[Double] = {
    val rowCount    = matrices.headOption.flatMap(_.headOption.map(_.length)).getOrElse(0)
    val colCount    = matrices.headOption.map(_.length).getOrElse(0)
    val matrixCount = matrices.size

    matrices.foldLeft(List.fill(rowCount, colCount)(0.0)) {
      _.zip(_).map { case (row1, row2) =>
        row1.zip(row2).map { case (el1, el2) => el1 + el2 / matrixCount }
      }
    }
  }
}

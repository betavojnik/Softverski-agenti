package com.github.betavojnik.models

import com.github.betavojnik.Matrix2D

final case class ModelData(weights: List[Matrix2D[Double]], biases: List[List[Double]])

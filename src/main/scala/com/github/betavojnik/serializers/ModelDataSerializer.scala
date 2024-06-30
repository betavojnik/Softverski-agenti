package com.github.betavojnik.serializers

import com.github.betavojnik.models.ModelData
import models.ModelData.{List => ListProto, Matrix2D => Matrix2DProto, ModelData => ModelDataProto}
import org.apache.pekko.serialization.SerializerWithStringManifest

class ModelDataSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 100001

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case ModelData(weights, biases) =>
      val protoWeights = weights.map { row =>
        Matrix2DProto(row.map(ListProto(_)))
      }
      val protoBiases = biases.map(ListProto(_))

      ModelDataProto(protoWeights, protoBiases).toByteArray

    case _ => throw new IllegalArgumentException(s"Cannot serialize object of type ${o.getClass}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    val protoModelData = ModelDataProto.parseFrom(bytes)

    val weights = protoModelData.weights.map(_.rows.map(_.values.toList).toList).toList
    val biases  = protoModelData.biases.map(_.values.toList).toList

    ModelData(weights, biases)
  }
}

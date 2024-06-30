package com.github.betavojnik.services

import com.github.betavojnik.models.ModelData
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.{PyQuote, SeqConverters}
import org.slf4j.Logger

class TrainerService(logger: Logger) {
  private val pd              = py.module("pandas")
  private val np              = py.module("numpy")
  private val preprocessing   = py.module("sklearn.preprocessing")
  private val neural_network  = py.module("sklearn.neural_network")
  private val metrics         = py.module("sklearn.metrics")
  private val model_selection = py.module("sklearn.model_selection")

  private def MLPClassifierPretrained(coefInit: py.Any, interceptInit: py.Any): py.Dynamic =
    py.Dynamic.global.`type`(
      "CallbackHandler",
      py"(${neural_network.MLPClassifier}, )",
      Map(
        "coef_init"      -> coefInit,
        "intercept_init" -> interceptInit,
        "_init_coef"     -> py"lambda self, fan_in, fan_out, dtype: [self.coef_init[1] if fan_out == 1 else self.coef_init[0], self.intercept_init[1] if fan_out == 1 else self.intercept_init[0]]"
      )
    )

  def train(initialData: Option[ModelData]): ModelData = {
    val data = pd.read_csv("mushroom_cleaned.csv")

    val X = data.drop(columns = Seq("class").toPythonProxy)
    val y = data.bracketAccess("class")

    val splitData = model_selection.train_test_split(X, y, test_size = 0.2, random_state = 42)
    val X_train   = splitData.bracketAccess(0)
    val X_test    = splitData.bracketAccess(1)
    val y_train   = splitData.bracketAccess(2)
    val y_test    = splitData.bracketAccess(3)

    val scaler         = preprocessing.StandardScaler()
    val X_train_scaled = scaler.fit_transform(X_train)
    val X_test_scaled  = scaler.transform(X_test)

    val model = initialData.fold(
      neural_network.MLPClassifier(hidden_layer_sizes = Seq(100).toPythonProxy, max_iter = 5)
    ) { data =>
      py"${MLPClassifierPretrained(data.weights.map(x => np.array(x.toPythonCopy)).toPythonCopy, data.biases.map(x => np.array(x.toPythonCopy)).toPythonCopy)}(hidden_layer_sizes = [100], max_iter = 5)"
    }
    model.fit(X_train_scaled, y_train)

    val y_pred = model.predict(X_test_scaled)

    logger.debug("Classification Report:\n" + metrics.classification_report(y_test, y_pred).toString)

    val networkWeights = model.coefs_.as[List[List[List[Double]]]]
    val networkBiases  = model.intercepts_.as[List[List[Double]]]

    ModelData(networkWeights, networkBiases)
  }
}

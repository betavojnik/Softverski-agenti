package com.github.betavojnik.services

import com.github.betavojnik.models.ModelData
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.{PyQuote, SeqConverters}
import org.slf4j.Logger

class TrainerService(logger: Logger) {
  private val pd              = py.module("pandas")
  private val preprocessing   = py.module("sklearn.preprocessing")
  private val neural_network  = py.module("sklearn.neural_network")
  private val metrics         = py.module("sklearn.metrics")
  private val model_selection = py.module("sklearn.model_selection")

  def train(initialData: Option[ModelData]): ModelData = {
    val data = pd.read_csv("mushroom_cleaned.csv")

    logger.debug(data.isnull().sum().toString)

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
    ) { _ =>
      py"MLPClassifierPretrained(coef_init = 1, intercept_init = 1, hidden_layer_sizes = Seq(100).toPythonProxy, max_iter = 5)"
    }
    model.fit(X_train_scaled, y_train)

    val y_pred = model.predict(X_test_scaled)

    logger.debug("Classification Report:\n" + metrics.classification_report(y_test, y_pred).toString)

    val coefs          = py"list(${model.coefs_})"
    val intercepts     = py"list(${model.intercepts_})"
    val networkWeights = coefs.as[List[List[List[Double]]]]
    val networkBiases  = intercepts.as[List[List[Double]]]

    networkWeights.zipWithIndex.foreach { case (layerWeights, index) =>
      logger.debug(s"Layer ${index + 1} weights: $layerWeights")
    }

    networkBiases.zipWithIndex.foreach { case (layerBiases, index) =>
      logger.debug(s"Layer ${index + 1} biases: $layerBiases")
    }

    ModelData(networkWeights, networkBiases)
  }
}

object TrainerService {
  py"""
    class MLPClassifierPretrained(MLPClassifier):
      def __init__(self, coef_init, intercept_init, hidden_layer_sizes = None, max_iter = None):
        super().__init__(hidden_layer_sizes = hidden_layer_sizes, max_iter = max_iter)
        self.coef_init = coef_init
        self.intercept_init = intercept_init

      def _init_coef(self, fan_in, fan_out):
        if self.activation == 'logistic':
          init_bound = np.sqrt(2. / (fan_in + fan_out))
        elif self.activation in ('identity', 'tanh', 'relu'):
          init_bound = np.sqrt(6. / (fan_in + fan_out))
        else:
          raise ValueError("Unknown activation function %s" % self.activation)

        return coef_init, intercept_init"""
}

package com.github.betavojnik.services

import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters
import me.shadaj.scalapy.py.PyQuote
import me.shadaj.scalapy.py.PyFunction

class TrainerService {
  def train(): List[List[Double]] = {
    val pd = py.module("pandas")
    val preprocessing = py.module("sklearn.preprocessing") //sklearn.preprocessing.StandardScaler
    val neural_network = py.module("sklearn.neural_network") //sklearn.neural_network.MLPClassifier
    val metrics = py.module("sklearn.metrics") //sklearn.metrics.accuracy_score
    val model_selection = py.module("sklearn.model_selection") //sklearn.model_selection.train_test_split

    val data = pd.read_csv("mushroom_cleaned.csv")

    println(data.isnull().sum())

    val X = data.drop(columns = Seq("class").toPythonProxy)
    val y = data.bracketAccess("class")

    val splitData = model_selection.train_test_split(X, y, test_size = 0.2, random_state = 42)
    val X_train = splitData.bracketAccess(0)
    val X_test = splitData.bracketAccess(1)
    val y_train = splitData.bracketAccess(2)
    val y_test = splitData.bracketAccess(3)

    val scaler = preprocessing.StandardScaler()
    val X_train_scaled = scaler.fit_transform(X_train)
    val X_test_scaled = scaler.transform(X_test)

    val model = neural_network.MLPClassifier(hidden_layer_sizes = Seq(50).toPythonProxy, max_iter = 200, random_state = 42)
    model.fit(X_train_scaled, y_train)

    val y_pred = model.predict(X_test_scaled)

    println("Classification Report:")
    println(metrics.classification_report(y_test, y_pred))

    val coefs = py"list(${model.coefs_})"
    val networkWeights = coefs.as[Seq[Seq[Seq[Double]]]]

    networkWeights.zipWithIndex.foreach { case (layerWeights, index) =>
      println(s"Layer ${index + 1} weights: \n")
      layerWeights.foreach { neuronWeights =>
        println(neuronWeights)
      }
      println()
    }

    val listNetworkWeights: List[List[Double]] = networkWeights.flatten.map(_.toList).toList
    listNetworkWeights

  }
}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "Softverski-agenti",
    libraryDependencies ++= List(
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "org.apache.pekko" %% "pekko-actor-typed" % "1.0.2",
      "org.apache.pekko" %% "pekko-cluster-typed" % "1.0.2"
    )
  )

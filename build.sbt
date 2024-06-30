Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

addCommandAlias("check", "fixCheck; fmtCheck")
addCommandAlias("fix", "scalafixAll")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("prepare", "fix; fmt")

fork := true

import ai.kien.python.Python

lazy val python = Python()

lazy val javaOpts = python.scalapyProperties.get.map { case (k, v) =>
  s"""-D$k=$v"""
}.toSeq

javaOptions ++= javaOpts

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

lazy val root = (project in file("."))
  .settings(
    name              := "Softverski-agenti",
    semanticdbEnabled := true,
    semanticdbOptions += "-P:semanticdb:synthetics:on",
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions := List(
      "-Wunused:imports",
      "-Wunused"
    ),
    libraryDependencies ++= List(
      "ch.qos.logback"    % "logback-classic"     % "1.5.6",
      "dev.scalapy"      %% "scalapy-core"        % "0.5.3",
      "org.apache.pekko" %% "pekko-actor-typed"   % "1.0.2",
      "org.apache.pekko" %% "pekko-cluster-typed" % "1.0.2"
    )
  )

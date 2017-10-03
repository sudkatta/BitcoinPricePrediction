
name := """simple-rest-scala"""

version := "1.0-SNAPSHOT"

lazy val root = project.in(file(".")).enablePlugins(PlayScala)

fork in run := false

libraryDependencies ++= Seq(ws)

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.2.0"

libraryDependencies += "org.codehaus.janino" % "janino" % "3.0.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % Test


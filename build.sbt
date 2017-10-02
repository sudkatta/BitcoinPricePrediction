name := """simple-rest-scala"""

version := "1.0-SNAPSHOT"

lazy val root = project.in(file(".")).enablePlugins(PlayScala)

fork in run := true

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.4.1"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.4.1"

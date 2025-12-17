name := "rcs-poller-lambda"

organization := "com.gu"

description:= "A lambda to poll RCS"

version := "1.0"

scalaVersion := "2.13.18"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:21",
  "-Ywarn-dead-code"
)

val awsVersion = "2.40.10"
val circeVersion = "0.14.14"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.14.0",
  "com.amazonaws" % "aws-lambda-java-core" % "1.4.0",
  "software.amazon.awssdk" % "cloudwatch" % awsVersion,
  "software.amazon.awssdk" % "dynamodb" % awsVersion,
  "software.amazon.awssdk" % "s3" % awsVersion,
  "org.scanamo" %% "scanamo" % "6.0.0",
  "org.playframework" %% "play-ahc-ws-standalone" % "3.0.9",
  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.slf4j" % "slf4j-simple" % "2.0.17"
)

enablePlugins(JavaAppPackaging)

Universal / topLevelDirectory := None
Universal / packageName := normalizedName.value

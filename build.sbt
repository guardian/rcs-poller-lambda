name := "rcs-poller-lambda"

organization := "com.gu"

description:= "A lambda to poll RCS"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
  "com.amazonaws" % "aws-java-sdk-sns" % "1.11.335",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.335",
  "io.circe" %% "circe-core" % "0.9.3",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.2",
  "com.amazonaws" % "aws-java-sdk-config" %  "1.11.163",
  "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25"
)

enablePlugins(RiffRaffArtifact)

assemblyJarName := s"${name.value}.jar"
riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), s"${name.value}-cfn/cfn.yaml")
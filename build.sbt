name := "rcs-poller-lambda"

organization := "com.gu"

description:= "A lambda to poll RCS"

version := "1.0"

scalaVersion := "2.13.18"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11",
  "-Ywarn-dead-code"
)

val awsVersion = "1.12.679"
val circeVersion = "0.14.14"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-cloudwatch" % awsVersion,
  "com.amazonaws" % "amazon-kinesis-client" % "1.15.1",
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "software.amazon.awssdk" % "dynamodb" % "2.25.9",
  "org.scanamo" %% "scanamo" % "1.0.0",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.2.6",
  "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.slf4j" % "slf4j-simple" % "2.0.9"
)

assemblyJarName := s"${name.value}.jar"

assembly / assemblyMergeStrategy := {
  // using two different versions of AWS SDK gives us duplicates in mime.types
  // we just need to keep one of them (either one) in the jar
  case "mime.types" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last == "deriving.conf" => MergeStrategy.filterDistinctLines
  case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
  case PathList("google", "protobuf", "struct.proto") => MergeStrategy.last
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

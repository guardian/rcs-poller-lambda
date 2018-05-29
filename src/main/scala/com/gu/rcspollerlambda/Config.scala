package com.gu.rcspollerlambda

import java.util.Properties

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth._
import com.amazonaws.regions.Regions
import com.gu.rcspollerlambda.S3._

import scala.util.Try

trait Config {
  val awsRegion = Regions.EU_WEST_1
  val stage = Option(System.getenv("Stage")).getOrElse("DEV")

  lazy val awsCredentials = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new ProfileCredentialsProvider("composer"),
    new InstanceProfileCredentialsProvider(false),
    new DefaultAWSCredentialsProviderChain)

  private lazy val s3Client = getS3Client(awsCredentials)
  private lazy val config = loadConfig()
  private def getConfig(property: String) = Option(config.getProperty(property)) getOrElse sys.error(s"'$property' property missing.")

  lazy val rcsUrl = getConfig("rcs.url")

  private def loadConfig() = {
    val configFileKey = s"$stage/config.properties"
    val configInputStream = s3Client.getObject("rcs-poller-lambda-config", configFileKey)
    val context2 = configInputStream.getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(context2)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}

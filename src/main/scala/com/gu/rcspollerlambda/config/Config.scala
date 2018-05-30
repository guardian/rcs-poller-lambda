package com.gu.rcspollerlambda.config

import java.util.Properties

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.gu.rcspollerlambda.services.Logging
import com.gu.rcspollerlambda.services.S3._

import scala.util.Try

trait Config extends Logging {
  val awsRegion = Regions.EU_WEST_1
  val stage = Option(System.getenv("Stage")).getOrElse("DEV")

  lazy val awsCredentials = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new ProfileCredentialsProvider("composer"),
    new InstanceProfileCredentialsProvider(false),
    new DefaultAWSCredentialsProviderChain)

  lazy val s3Client = getS3Client(awsCredentials)
  private lazy val config = loadConfig()
  private def getConfig(property: String) = Option(config.getProperty(property)) getOrElse sys.error(s"'$property' property missing.")

  lazy val rcsUrl = getConfig("rcs.url")
  lazy val loggingStreamName = getConfig("logging.stream")
  lazy val elkLoggingEnabled = stage != "DEV"

  private def loadConfig() = {
    val configFile: Properties = new Properties()
    try {
      val configInputStream = s3Client.getObject("rcs-poller-lambda-config", s"$stage/config.properties")
      val context2 = configInputStream.getObjectContent
      Try(configFile.load(context2)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
      configFile
    } catch {
      case e: Throwable =>
        logger.info(s"Error while getting config from S3 bucket: $e")
        configFile
    }
  }
}

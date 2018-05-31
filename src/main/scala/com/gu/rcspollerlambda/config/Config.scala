package com.gu.rcspollerlambda.config

import java.util.Properties

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.amazonaws.services.sns.{ AmazonSNS, AmazonSNSClientBuilder }
import com.gu.rcspollerlambda.services.Logging

import scala.util.Try

trait Config extends Logging {
  object AWS {
    val awsRegion = Regions.EU_WEST_1

    lazy val awsCredentials = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new ProfileCredentialsProvider("composer"),
      new InstanceProfileCredentialsProvider(false),
      new DefaultAWSCredentialsProviderChain)

    lazy val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(awsCredentials).build()
    lazy val snsClient: AmazonSNS = AmazonSNSClientBuilder.standard().withRegion(awsRegion).withCredentials(awsCredentials).build()

    lazy val topicArn: String = getConfig("sns.topic.arn")
  }

  lazy val stage: String = Option(System.getenv("Stage")).getOrElse("DEV")

  lazy val rcsUrl: String = getConfig("rcs.url")

  private lazy val config = loadConfig()
  private def getConfig(property: String) = Option(config.getProperty(property)) getOrElse sys.error(s"'$property' property missing.")
  private def loadConfig() = {
    val configFile: Properties = new Properties()
    try {
      val configInputStream = AWS.s3Client.getObject("rcs-poller-lambda-config", s"$stage/config.properties")
      val context2 = configInputStream.getObjectContent
      Try(configFile.load(context2)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
      configFile
    } catch {
      case e: Throwable =>
        logger.error(s"Error while getting config from S3 bucket: ${e.getMessage}")
        configFile
    }
  }
}

package com.gu.rcspollerlambda.config

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchAsync, AmazonCloudWatchAsyncClientBuilder}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.gu.rcspollerlambda.models.LambdaError
import com.gu.rcspollerlambda.services.S3

import java.util.Properties

object Config {
  object AWS {
    val awsRegion = Regions.EU_WEST_1

    lazy val dynamoTableName: String = s"rcs-poller-lambda-$stage"

    lazy val awsComposerCredentials = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new ProfileCredentialsProvider("composer"),
      new InstanceProfileCredentialsProvider(false))

    lazy val dynamoClient: AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard
      .withRegion(awsRegion)
      .withCredentials(awsComposerCredentials)
      .build()
    lazy val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard
      .withRegion(awsRegion)
      .withCredentials(awsComposerCredentials)
      .build()
    lazy val cloudwatchClient: AmazonCloudWatchAsync = AmazonCloudWatchAsyncClientBuilder.standard
      .withCredentials(awsComposerCredentials)
      .withEndpointConfiguration(new EndpointConfiguration(Region.getRegion(awsRegion).getServiceEndpoint(AmazonCloudWatch.ENDPOINT_PREFIX), awsRegion.getName))
      .build()
  }

  lazy val stage: String = Option(System.getenv("Stage")).getOrElse("DEV")

  //RCS endpoint
  lazy val rcsUrl: String = getConfig("rcs.url")
  lazy val subscriberName: String = getConfig("rcs.subscriber.name")

  //MetadataService endpoint
  lazy val metadataServiceDomain: String = getConfig("metadataservice.domain")
  lazy val metadataServiceApiKey: String = getConfig("metadataservice.key")

  //Switches
  lazy val isRcsEnabled: Boolean = getConfig("rcs.enabled").toBoolean
  lazy val isMetadataServiceEnabled: Boolean = getConfig("metadataservice.enabled").toBoolean

  private lazy val config: Either[LambdaError, Properties] = S3.loadConfig()
  private def getConfig(property: String): String = config
    .map(c => Option(c.getProperty(property)).getOrElse(sys.error(s"'$property' property missing.")))
    .fold(err => sys.error(err.message), identity)
}

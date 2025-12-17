package com.gu.rcspollerlambda.config

import java.util.Properties
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProviderChain,
  DefaultCredentialsProvider,
  ProfileCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import com.gu.rcspollerlambda.models.LambdaError
import com.gu.rcspollerlambda.services.S3
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.s3.S3Client

object Config {
  object AWS {
    lazy val dynamoTableName: String = s"rcs-poller-lambda-$stage"

    private lazy val awsComposerCredentials =
      AwsCredentialsProviderChain.of(
        ProfileCredentialsProvider.create("composer"),
        DefaultCredentialsProvider.builder().build()
      )

    lazy val dynamoClient = DynamoDbClient
      .builder()
      .credentialsProvider(awsComposerCredentials)
      .region(Region.EU_WEST_1)
      .build()

    lazy val s3Client = S3Client
      .builder()
      .credentialsProvider(awsComposerCredentials)
      .region(Region.EU_WEST_1)
      .build()

    lazy val cloudwatchClient = CloudWatchClient
      .builder()
      .credentialsProvider(awsComposerCredentials)
      .region(Region.EU_WEST_1)
      .build()

  }

  lazy val stage: String = Option(System.getenv("Stage")).getOrElse("DEV")

  // RCS endpoint
  lazy val rcsUrl: String = getConfig("rcs.url")
  lazy val subscriberName: String = getConfig("rcs.subscriber.name")

  // MetadataService endpoint
  lazy val metadataServiceDomain: String = getConfig("metadataservice.domain")
  lazy val metadataServiceApiKey: String = getConfig("metadataservice.key")

  // Switches
  lazy val isRcsEnabled: Boolean = getConfig("rcs.enabled").toBoolean
  lazy val isMetadataServiceEnabled: Boolean = getConfig(
    "metadataservice.enabled"
  ).toBoolean

  private lazy val config: Either[LambdaError, Properties] = S3.loadConfig()
  private def getConfig(property: String): String = config
    .map(c =>
      Option(c.getProperty(property))
        .getOrElse(sys.error(s"'$property' property missing."))
    )
    .fold(err => sys.error(err.message), identity)
}

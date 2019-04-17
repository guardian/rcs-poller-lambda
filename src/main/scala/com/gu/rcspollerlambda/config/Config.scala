package com.gu.rcspollerlambda.config

import java.util.Properties

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchAsync, AmazonCloudWatchAsyncClientBuilder}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder}
import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClient}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.securitytoken.{AWSSecurityTokenService, AWSSecurityTokenServiceClientBuilder}
import com.gu.rcspollerlambda.models.LambdaError
import com.gu.rcspollerlambda.services.{Logging, S3}

trait Config extends Logging {
  object AWS {
    val awsRegion = Regions.EU_WEST_1

    lazy val roleArn: String = getConfig("role.arn")
    lazy val kinesisStreamName: String = getConfig("kinesis.stream")
    lazy val dynamoTableName: String = s"rcs-poller-lambda-$stage"

    lazy val awsComposerCredentials = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new ProfileCredentialsProvider("composer"),
      new InstanceProfileCredentialsProvider(false))

    lazy val dynamoClient: AmazonDynamoDBAsync = AmazonDynamoDBAsyncClientBuilder.standard
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
    lazy val kinesisClient: AmazonKinesis = AmazonKinesisClient.builder()
      .withRegion(awsRegion)
      .withCredentials(getMediaServiceCredentials)
      .build()

    // We need to assume sts role in order to get creds to send messages on the kinesis stream in the media-service account
    private def getMediaServiceCredentials: STSAssumeRoleSessionCredentialsProvider = {
      lazy val stsClient: AWSSecurityTokenService = AWSSecurityTokenServiceClientBuilder.standard().withRegion(awsRegion).build()
      new STSAssumeRoleSessionCredentialsProvider.Builder().withStsClient(stsClient).build()
    }
  }

  lazy val stage: String = Option(System.getenv("Stage")).getOrElse("DEV")

  lazy val rcsUrl: String = getConfig("rcs.url")
  lazy val subscriberName: String = getConfig("rcs.subscriber.name")

  lazy val isRcsEnabled: Boolean = getConfig("rcs.enabled").toBoolean
  lazy val isKinesisEnabled: Boolean = getConfig("kinesis.enabled").toBoolean

  private lazy val config: Either[LambdaError, Properties] = S3.loadConfig()
  private def getConfig(property: String): String = config
    .map(c => Option(c.getProperty(property)).getOrElse(sys.error(s"'$property' property missing.")))
    .fold(err => sys.error(err.message), identity)
}

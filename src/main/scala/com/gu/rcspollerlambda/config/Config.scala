package com.gu.rcspollerlambda.config

import java.util.Properties

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchAsyncClientBuilder}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder}
import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClient}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.securitytoken.model.{AssumeRoleRequest, Credentials}
import com.amazonaws.services.securitytoken.{AWSSecurityTokenService, AWSSecurityTokenServiceClientBuilder}
import com.gu.rcspollerlambda.models.LambdaError
import com.gu.rcspollerlambda.services.{Logging, S3}

trait Config extends Logging {
  object AWS {
    val awsRegion = Regions.EU_WEST_1

    lazy val awsComposerCredentials = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new ProfileCredentialsProvider("composer"),
      new InstanceProfileCredentialsProvider(false))

    lazy val dynamoClient: AmazonDynamoDBAsync = AmazonDynamoDBAsyncClientBuilder.standard.withCredentials(awsComposerCredentials).withRegion(awsRegion).build()
    lazy val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard.withRegion(awsRegion).withCredentials(awsComposerCredentials).build()
    lazy val cloudwatchClient = AmazonCloudWatchAsyncClientBuilder.standard
      .withCredentials(awsComposerCredentials)
      .withEndpointConfiguration(new EndpointConfiguration(Region.getRegion(awsRegion).getServiceEndpoint(AmazonCloudWatch.ENDPOINT_PREFIX), awsRegion.getName))
      .build()

    // We need to assume sts role in order to send messages to the kinesis stream in the media-service
    lazy val roleArn: String = getConfig("role.arn")
    lazy val stsClient: AWSSecurityTokenService = AWSSecurityTokenServiceClientBuilder.standard().withRegion(awsRegion).build()
    lazy val roleCredentials: Credentials = stsClient.assumeRole(new AssumeRoleRequest()
      .withRoleArn(roleArn)
      .withRoleSessionName("cross-account-lambda-write-to-kinesis")).getCredentials
    lazy val awsMediaServiceCredentials = new BasicSessionCredentials(roleCredentials.getAccessKeyId, roleCredentials.getSecretAccessKey, roleCredentials.getSessionToken)
    lazy val kinesisClient: AmazonKinesis = AmazonKinesisClient.builder()
      .withRegion(awsRegion)
      .withCredentials(new AWSStaticCredentialsProvider(awsMediaServiceCredentials))
      .build()

    lazy val kinesisStreamName: String = getConfig("kinesis.stream")
    lazy val dynamoTableName: String = s"rcs-poller-lambda-$stage"
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

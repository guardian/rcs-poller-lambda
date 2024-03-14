package com.gu.rcspollerlambda.config

import java.util.Properties
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.cloudwatch.{ AmazonCloudWatch, AmazonCloudWatchAsync, AmazonCloudWatchAsyncClientBuilder }
import com.amazonaws.services.kinesis.{ AmazonKinesis, AmazonKinesisClient }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.amazonaws.services.securitytoken.{ AWSSecurityTokenService, AWSSecurityTokenServiceClientBuilder }
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProviderChain => AwsCredentialsProviderChainV2, DefaultCredentialsProvider => DefaultCredentialsProviderV2, ProfileCredentialsProvider => ProfileCredentialsProviderV2 }
import software.amazon.awssdk.regions.{ Region => RegionV2 }
import com.gu.rcspollerlambda.models.LambdaError
import com.gu.rcspollerlambda.services.S3

object Config {
  object AWS {
    val awsRegion = Regions.EU_WEST_1

    lazy val roleArn: String = getConfig("role.arn")
    lazy val kinesisStreamName: String = getConfig("kinesis.stream")
    lazy val dynamoTableName: String = s"rcs-poller-lambda-$stage"

    lazy val awsComposerCredentials = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new ProfileCredentialsProvider("composer"),
      new InstanceProfileCredentialsProvider(false))

    private lazy val awsComposerCredentialsV2 = AwsCredentialsProviderChainV2.of(
      ProfileCredentialsProviderV2.create("mobile"),
      DefaultCredentialsProviderV2.create())

    lazy val dynamoClient = DynamoDbClient.builder().credentialsProvider(awsComposerCredentialsV2).region(RegionV2.EU_WEST_1).build()

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
      new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, "RCS-poller-lambda").withStsClient(stsClient).build()
    }
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
  lazy val isKinesisEnabled: Boolean = getConfig("kinesis.enabled").toBoolean
  lazy val isMetadataServiceEnabled: Boolean = getConfig("metadataservice.enabled").toBoolean

  private lazy val config: Either[LambdaError, Properties] = S3.loadConfig()
  private def getConfig(property: String): String = config
    .map(c => Option(c.getProperty(property)).getOrElse(sys.error(s"'$property' property missing.")))
    .fold(err => sys.error(err.message), identity)
}

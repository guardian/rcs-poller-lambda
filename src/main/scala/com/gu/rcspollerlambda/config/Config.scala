package com.gu.rcspollerlambda.config

import java.util.Properties

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.amazonaws.services.sns.{ AmazonSNS, AmazonSNSClientBuilder }
import com.gu.rcspollerlambda.services.{ Logging, S3 }

trait Config extends Logging {
  object AWS {
    val awsRegion = Regions.EU_WEST_1

    lazy val awsCredentials = new AWSCredentialsProviderChain(
      new EnvironmentVariableCredentialsProvider(),
      new ProfileCredentialsProvider("composer"),
      new InstanceProfileCredentialsProvider(false),
      new DefaultAWSCredentialsProviderChain)

    lazy val dynamoClient: AmazonDynamoDBAsync = AmazonDynamoDBAsyncClientBuilder.standard.withCredentials(awsCredentials).withRegion(awsRegion).build()
    lazy val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard.withRegion(Regions.EU_WEST_1).withCredentials(awsCredentials).build()
    lazy val snsClient: AmazonSNS = AmazonSNSClientBuilder.standard.withRegion(awsRegion).withCredentials(awsCredentials).build()

    lazy val topicArn: String = getConfig("sns.topic.arn")
    lazy val dynamoTableName: String = getConfig("dynamodb.table.name")
  }

  lazy val stage: String = Option(System.getenv("Stage")).getOrElse("DEV")

  lazy val rcsUrl: String = getConfig("rcs.url")

  private lazy val config: Either[LambdaError, Properties] = S3.loadConfig()
  private def getConfig(property: String): String = {
    config
      .map(c => Option(c.getProperty(property)).getOrElse(sys.error(s"'$property' property missing.")))
      .fold(err => sys.error(err.message), identity)
  }
}

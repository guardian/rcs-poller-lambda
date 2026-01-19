package com.gu.rcspollerlambda.services

import java.util.Properties

import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.models.{LambdaError, S3DownloadError}
import software.amazon.awssdk.services.s3.model.GetObjectRequest

object S3 extends Logging {
  // For DEV only
  def getXmlFile: Either[LambdaError, String] = {
    val file = s"rcs-poller-lambda-config/$stage/example.xml"
    logger.info(s"Loading XML from S3 bucket: $file")
    try {
      val getObjectRequest = GetObjectRequest
        .builder()
        .bucket("rcs-poller-lambda-config")
        .key(s"$stage/example.xml")
        .build()
      val xmlInputStream = AWS.s3Client
        .getObject(getObjectRequest)
      val xmlAsString = scala.io.Source
        .fromInputStream(xmlInputStream)
        .getLines()
        .mkString("\n")
      xmlInputStream.close()
      Right(xmlAsString)
    } catch {
      case e: Throwable => Left(S3DownloadError(file, e.getMessage))
    }
  }

  def loadConfig(): Either[LambdaError, Properties] = {
    val file = s"rcs-poller-lambda-config/$stage/config.properties"
    try {
      val configFile: Properties = new Properties()
      val bucket = "rcs-poller-lambda-config"
      val path = s"$stage/config.properties"
      logger.info(s"Loading config from s3://$bucket/$path")
      val getObjectRequest =
        GetObjectRequest.builder().bucket(bucket).key(path).build()
      val configInputStream = AWS.s3Client.getObject(getObjectRequest)
      configFile.load(configInputStream)
      configInputStream.close()
      Right(configFile)
    } catch {
      case e: Throwable => Left(S3DownloadError(file, e.getMessage))
    }
  }
}

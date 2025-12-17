package com.gu.rcspollerlambda.services

import java.util.Properties

import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.models.{LambdaError, S3DownloadError}

object S3 extends Logging {
  // For DEV only
  def getXmlFile: Either[LambdaError, String] = {
    val file = s"rcs-poller-lambda-config/$stage/example.xml"
    logger.info(s"Loading XML from S3 bucket: $file")
    try {
      val xmlInputStream = AWS.s3Client
        .getObject("rcs-poller-lambda-config", s"$stage/example.xml")
        .getObjectContent
      val xmlAsString = scala.io.Source
        .fromInputStream(xmlInputStream)
        .getLines()
        .mkString("\n")
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
      val configInputStream = AWS.s3Client.getObject(bucket, path)
      val context2 = configInputStream.getObjectContent
      configFile.load(context2)
      Right(configFile)
    } catch {
      case e: Throwable => Left(S3DownloadError(file, e.getMessage))
    }
  }
}

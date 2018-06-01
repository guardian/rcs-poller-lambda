package com.gu.rcspollerlambda.services

import java.util.Properties

import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.gu.rcspollerlambda.config.{ Config, LambdaError, S3DownloadError }

object S3 extends Config {
  // For DEV only
  def getXmlFile: Either[LambdaError, S3ObjectInputStream] = {
    val file = "cs-poller-lambda-config/example.xml"
    logger.info(s"Loading XML from S3 bucket: $file")
    try {
      val xmlInputStream = AWS.s3Client.getObject("rcs-poller-lambda-config", "example.xml")
      Right(xmlInputStream.getObjectContent)
    } catch {
      case e: Throwable => Left(S3DownloadError(file, e.getMessage))
    }
  }

  def loadConfig(): Either[LambdaError, Properties] = {
    val file = s"rcs-poller-lambda-config/$stage/config.properties"
    try {
      val configFile: Properties = new Properties()
      val configInputStream = AWS.s3Client.getObject("rcs-poller-lambda-config", s"$stage/config.properties")
      val context2 = configInputStream.getObjectContent
      configFile.load(context2)
      Right(configFile)
    } catch {
      case e: Throwable => Left(S3DownloadError(file, e.getMessage))
    }
  }
}

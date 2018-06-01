package com.gu.rcspollerlambda.services

import java.util.Properties

import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.gu.rcspollerlambda.config.Config

import scala.util.Try

object S3 extends Config {
  // For DEV only
  def getXmlFile: Either[String, S3ObjectInputStream] = {
    try {
      val xmlInputStream = AWS.s3Client.getObject("rcs-poller-lambda-config", "example.xml")
      logger.info(s"Loading XML from S3 bucket: ${xmlInputStream.getBucketName}/${xmlInputStream.getKey}")

      Right(xmlInputStream.getObjectContent)
    } catch {
      case e: Throwable => Left(s"Error while downloading XML file: ${e.getMessage}")
    }
  }

  def loadConfig() = {
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

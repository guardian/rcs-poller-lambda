package com.gu.rcspollerlambda.services

import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.gu.rcspollerlambda.config.Config

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
  def getLastId = "26400822"
}

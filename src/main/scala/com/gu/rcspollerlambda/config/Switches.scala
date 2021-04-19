package com.gu.rcspollerlambda.config

import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.models.LambdaError
import com.gu.rcspollerlambda.services.{ Logging, S3 }

object Switches extends Logging {
  def rcsEnabled(action: => Either[LambdaError, String]): Either[LambdaError, String] =
    if (isRcsEnabled) action
    else {
      logger.info(s"Hitting RCS endpoint not enabled, reading from S3 file instead...")
      S3.getXmlFile
    }

  def kinesisEnabled(action: => Either[LambdaError, String]): Either[LambdaError, String] =
    if (isKinesisEnabled) action
    else {
      logger.info(s"Sending rights on the Kinesis stream is not enabled...")
      Right("Sending rights to Kinesis is not enabled")
    }

  def metadataServiceEnabled(action: => Either[LambdaError, String]): Either[LambdaError, String] =
    if (isMetadataServiceEnabled) action
    else {
      logger.info(s"Sending rights to the Metadata service is not enabled...")
      Right("Sending rights to the Metadata service is not enabled")
    }

}

package com.gu.rcspollerlambda.config

import com.gu.rcspollerlambda.models.LambdaError
import com.gu.rcspollerlambda.services.{ Logging, S3 }

object Switches extends Config with Logging {
  def rcsEnabled(action: => Either[LambdaError, String]): Either[LambdaError, String] =
    if (isRcsEnabled) action
    else {
      logger.info(s"Hitting RCS endpoint not enabled, reading from S3 file instead...")
      S3.getXmlFile
    }

  def kinesisEnabled(action: => Either[LambdaError, Unit]): Either[LambdaError, Unit] =
    if (isKinesisEnabled) action
    else {
      logger.info(s"Sending rights on the Kinesis stream is not enabled...")
      Right(())
    }
}

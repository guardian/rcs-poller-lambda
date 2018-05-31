package com.gu.rcspollerlambda.services

import com.amazonaws.services.sns.model.PublishRequest
import com.gu.rcspollerlambda.config.Config
import io.circe.Json

object SNS extends Config with Logging {
  def publish(message: Json): Either[String, Unit] = {
    logger.info(s"Sending json to SNS stream...")
    try { Right(AWS.snsClient.publish(new PublishRequest(AWS.topicArn, message.toString(), "update-rcs-rights"))) } catch { case e: Throwable => Left(s"Error while sending message to SNS: ${e.getMessage}") }
  }
}

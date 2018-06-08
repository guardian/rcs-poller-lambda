package com.gu.rcspollerlambda.services

import cats.implicits._
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ LambdaError, SNSPublishError }
import io.circe.Json

object SNS extends Config with Logging {
  def publishRCSUpdates(rcsUpdates: List[Json]): Either[LambdaError, Unit] = {
    logger.info(s"Sending ${rcsUpdates.length} json RCS updates to the SNS stream...")
    rcsUpdates.map(publish).sequence_
  }

  private def publish(message: Json): Either[LambdaError, Unit] = {
    try {
      Right(AWS.snsClient.publish(new PublishRequest(AWS.topicArn, message.noSpaces, "update-rcs-rights")))
    } catch {
      case e: Throwable => Left(SNSPublishError(message.hcursor.downField("id").as[String].right.get, e.getMessage))
    }
  }
}

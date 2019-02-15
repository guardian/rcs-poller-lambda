package com.gu.rcspollerlambda.services

import com.amazonaws.services.sns.model.PublishRequest
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ LambdaError, SNSPublishError }
import io.circe.Json

object SNS extends Config with Logging {

  def publish(subject: String, message: Json): Either[LambdaError, Unit] = {
    try {
      Right(AWS.snsClient.publish(new PublishRequest(AWS.topicArn, message.noSpaces, subject)))
    } catch {
      case e: Throwable => Left(SNSPublishError(message.hcursor.downField("id").as[String].right.get, e.getMessage))
    }
  }

}

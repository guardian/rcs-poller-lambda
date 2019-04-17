package com.gu.rcspollerlambda.services

import java.nio.ByteBuffer
import java.util.UUID

import cats.implicits._
import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.{KinesisPublishError, LambdaError}
import io.circe.Json

object Kinesis extends Logging {
  def publishRCSUpdates(rcsUpdates: List[Json]): Either[LambdaError, Unit] = Switches.kinesisEnabled {
    logger.info(s"Sending ${rcsUpdates.length} json RCS update(s) to the Kinesis stream...")
    rcsUpdates.map(publish).sequence_
  }

  private def publish(message: Json): Either[LambdaError, Unit] = {
    try {
      val putRecordRequest = new PutRecordRequest()
        .withStreamName(AWS.kinesisStreamName)
        .withData(ByteBuffer.wrap(message.noSpaces.getBytes))
        .withPartitionKey(UUID.randomUUID().toString)

      Right(AWS.kinesisClient.putRecord(putRecordRequest))
    } catch {
      case e: Throwable => Left(KinesisPublishError(message.hcursor.downField("id").as[String].right.get, e.getMessage))
    }
  }
}

package com.gu.rcspollerlambda.services

import java.util.UUID

import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.amazonaws.services.kinesis.{ AmazonKinesis, AmazonKinesisClientBuilder }
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ KinesisPublishError, LambdaError }
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import io.circe.{ Encoder, Json, Printer }

object Kinesis extends Config with Logging {

  lazy val client: AmazonKinesis = AmazonKinesisClientBuilder.standard().
    withRegion(AWS.awsRegion).
    withCredentials(AWS.awsCredentials).
    build()

  def publish(subject: String, message: Json): Either[LambdaError, Unit] = {
    val updateMessage = UpdateMessage(subject = subject, rcsUpdate = message)

    val partitionKey = UUID.randomUUID().toString

    implicit val encoder: Encoder[UpdateMessage] = deriveEncoder[UpdateMessage]
    val printer = Printer.noSpaces.copy(dropNullValues = true)
    val payload = printer.prettyByteBuffer(updateMessage.asJson)

    try {
      Right {
        val request = new PutRecordRequest()
          .withStreamName(AWS.kinesisStream)
          .withPartitionKey(partitionKey)
          .withData(payload)

        client.putRecord(request)
      }
    } catch {
      case e: Throwable => Left(KinesisPublishError(partitionKey, e.getMessage))
    }
  }

}

case class UpdateMessage(subject: String, rcsUpdate: Json) // TODO not happy about the Json type can't bypass the top level toJson step
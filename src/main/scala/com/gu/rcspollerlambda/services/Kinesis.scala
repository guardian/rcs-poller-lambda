package com.gu.rcspollerlambda.services

import java.util.UUID

import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClientBuilder}
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{KinesisPublishError, LambdaError}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import io.circe.{Encoder, Json, Printer}

object Kinesis extends Config with Logging {

  lazy val client: AmazonKinesis = AmazonKinesisClientBuilder.standard().
    withRegion(AWS.awsRegion).
    withCredentials(AWS.awsCredentials).
    build()

  def publish(subject: String, message: Json): Either[LambdaError, Unit] = {

    def updateMessageFromJSON = {
      // message is a Json representation of a RCSUpdate
      // case class RCSUpdate(tagSetId: Long, id: String, data: SyndicationRights)
      //
      // Grid is expecting an image id on field 'id' and a SyndicationRights on field syndicationRights.
      // tagSetId is not consumed by the Grid

      val imageId: Option[String] = (message \\ "id").headOption.flatMap(_.asString)
      val syndicationRights: Option[Json] = (message \\ "data").headOption

      UpdateMessage(subject = subject, id = imageId, syndicationRights = syndicationRights)
    }

    val partitionKey = UUID.randomUUID().toString

    implicit val encoder: Encoder[UpdateMessage] = deriveEncoder[UpdateMessage]
    val printer = Printer.noSpaces.copy(dropNullValues = true)
    val payload = printer.prettyByteBuffer(updateMessageFromJSON.asJson)

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

case class UpdateMessage(subject: String, id: Option[String], syndicationRights: Option[Json]) // TODO not happy about the Json type can't bypass the top level toJson step
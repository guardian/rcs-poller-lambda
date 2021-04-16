package com.gu.rcspollerlambda.services

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.UUID
import java.util.zip.GZIPOutputStream

import cats.implicits._
import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.{ KinesisPublishError, LambdaError }
import io.circe.Json

object Kinesis extends Logging {
  def publishRCSUpdates(lastId: String, rcsUpdates: List[Json]): Either[LambdaError, String] = Switches.kinesisEnabled {
    logger.info(s"Sending ${rcsUpdates.length} json RCS update(s) to the Kinesis stream...")
    rcsUpdates.map(publish).collect { case Left(f) => f.message } match {
      case Nil => Right("All messages published to Kinesis")
      case errors => Left(KinesisPublishError(lastId, errors.mkString("\n")))
    }
  }

  private val compressionMarkerByte: Byte = 0x00.toByte

  private def compress(bytes: Array[Byte]): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream()
    val zipOutputStream = new GZIPOutputStream(outputStream)
    zipOutputStream.write(bytes)
    zipOutputStream.close()
    outputStream.close()
    val compressedBytes = outputStream.toByteArray
    compressionMarkerByte +: compressedBytes
  }

  private def publish(message: Json): Either[LambdaError, String] = {
    try {
      val putRecordRequest = new PutRecordRequest()
        .withStreamName(AWS.kinesisStreamName)
        .withData(ByteBuffer.wrap(compress(message.noSpaces.getBytes)))
        .withPartitionKey(UUID.randomUUID().toString)

      AWS.kinesisClient.putRecord(putRecordRequest)
      Right("Message published to Kinesis")
    } catch {
      case e: Throwable => Left(KinesisPublishError(message.hcursor.downField("id").as[String].right.get, e.getMessage))
    }
  }
}

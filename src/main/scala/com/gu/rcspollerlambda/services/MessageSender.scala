package com.gu.rcspollerlambda.services

import cats.implicits._
import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.LambdaError
import io.circe.Json

object MessageSender extends Logging {

  private val Subject = "upsert-rcs-rights"

  def publishRCSUpdates(rcsUpdates: List[Json]): Either[LambdaError, Unit] = Switches.snsEnabled {

    logger.info(s"Sending ${rcsUpdates.length} json RCS updates to the SNS stream...")
    val snsOutcomes = rcsUpdates.map(update => SNS.publish(Subject, update))

    logger.info(s"Sending ${rcsUpdates.length} RCS updates to the Kinesis stream...")
    val kinesisOutcomes = rcsUpdates.map(update => Kinesis.publish(Subject, update))

    (snsOutcomes ++ kinesisOutcomes).sequence_
  }

}
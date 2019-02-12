package com.gu.rcspollerlambda.services

import cats.implicits._
import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.LambdaError
import io.circe.Json

object MessageSender extends Logging {

  def publishRCSUpdates(rcsUpdates: List[Json]): Either[LambdaError, Unit] = Switches.snsEnabled {
    logger.info(s"Sending ${rcsUpdates.length} json RCS updates to the SNS stream...")
    rcsUpdates.map(SNS.publish).sequence_
  }

}
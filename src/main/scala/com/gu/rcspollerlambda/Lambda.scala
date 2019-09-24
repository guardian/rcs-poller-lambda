package com.gu.rcspollerlambda

import com.amazonaws.services.lambda.runtime.Context
import com.gu.rcspollerlambda.models.{ LambdaError, RightsBatch }
import com.gu.rcspollerlambda.services._

object Lambda extends Logging {
  /*
   * This is the lambda entry point
   */
  def handler(context: Context): Unit = {
    process()
  }

  def process(): Unit = {
    val newLastId: Either[LambdaError, Option[Long]] = for {
      lastId <- DynamoDB.getLastId
      body <- HTTP.getXml(lastId)
      xml <- XMLOps.stringToXml(body)
      rb <- XMLOps.xmlToRightsBatch(xml)
      json <- RightsBatch.toJsonMessage(rb.rightsUpdates)
      _ <- Kinesis.publishRCSUpdates(json)
    } yield rb.lastPosition

    newLastId.fold(
      err => {
        CloudWatch.publishError
        logger.error(err.message)
      },
      {
        case Some(id) =>
          DynamoDB.saveLastId(id)
          CloudWatch.publishOK
          logger.info(s"Lambda run successfully.")
        case None =>
          CloudWatch.publishOK
          logger.warn(s"No new rights tags, lambda will run with the same last id again.")
      })
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
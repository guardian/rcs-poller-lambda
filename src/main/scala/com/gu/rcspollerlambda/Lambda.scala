package com.gu.rcspollerlambda

import com.amazonaws.services.lambda.runtime.Context
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.RightsBatch
import com.gu.rcspollerlambda.services._

object Lambda extends Logging with Config {
  /*
   * This is the lambda entry point
   */
  def handler(context: Context): Unit = {
    process()
  }

  def process(): Unit = {
    val newLastId = for {
      lastId <- DynamoDB.getLastId
      body <- HTTP.getXml(lastId)
      xml <- XMLOps.stringToXml(body)
      rb <- XMLOps.xmlToRightsBatch(xml)
      json <- RightsBatch.toJson(rb.rightsUpdates)
      _ <- SNS.publishRCSUpdates(json)
    } yield rb.lastPosition

    newLastId.fold(
      err => {
        // TODO: Alert on error
        logger.error(err.message)
      },
      {
        case Some(id) =>
          DynamoDB.saveLastId(id)
          logger.info(s"Lambda run successfully.")
        case None =>
          // TODO: Alert on error
          logger.error(s"Missing id, lambda will run with the same last id again.")
      })
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
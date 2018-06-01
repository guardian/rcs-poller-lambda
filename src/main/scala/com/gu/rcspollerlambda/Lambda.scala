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
      lastid <- DynamoDB.getLastId
      body <- S3.getXmlFile // TODO: Use HTTP.getXml(lastid) when endpoint is ready
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
      lastid => {
        // TODO: Save lastid to db (once endpoint is ready)
        logger.info(s"Lambda run successfully.")
      })
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
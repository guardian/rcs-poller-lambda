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
    val lastPosition = for {
      lastid <- DynamoDB.getLastId
      body <- S3.getXmlFile
      xml <- XMLOps.stringToXml(body)
      rb <- XMLOps.xmlToRightsBatch(xml)
      json <- RightsBatch.toJson(rb)
      _ <- SNS.publish(json)
    } yield rb.lastPosition

    lastPosition.fold(
      err => {
        // TODO: Alert on error
        logger.error(err)
      },
      lastid => {
        // TODO: Save lastid to db
        logger.info(s"Lambda run successfully.")
      })
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
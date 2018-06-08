package com.gu.rcspollerlambda

import com.amazonaws.services.lambda.runtime.Context
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ LambdaError, RightsBatch }
import com.gu.rcspollerlambda.services._

object Lambda extends Logging with Config {
  /*
   * This is the lambda entry point
   */
  def handler(context: Context): Unit = {
    process()
  }

  private def fetchXml(id: String): Either[LambdaError, String] = stage match {
    case "PROD" => HTTP.getXml(id)
    case _ => S3.getXmlFile
  }

  def process(): Unit = {
    val newLastId = for {
      lastid <- DynamoDB.getLastId
      body <- fetchXml(lastid)
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
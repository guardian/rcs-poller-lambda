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

  def process(): Unit = {
    val newLastId = for {
      lastId <- DynamoDB.getLastId
      body <- fetchXml(lastId)
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
      lastId => {
        // TODO: Save lastId to db (once endpoint is ready)
        logger.info(s"Lambda run successfully.")
      })
  }

  private def fetchXml(id: String): Either[LambdaError, String] = stage match {
    case "PROD" => HTTP.getXml(id)
    case _ => S3.getXmlFile
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
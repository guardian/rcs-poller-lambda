package com.gu.rcspollerlambda

import com.amazonaws.services.lambda.runtime.Context
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.services._

object Lambda extends Logging with Config {
  /*
   * This is your lambda entry point
   */
  def handler(context: Context): Unit = {
    process()
  }

  def process(): Unit = stage match {
    case "DEV" =>
      val result = for {
        body <- S3.getXmlFile
        xml <- XMLOps.stringToXml(body)
        json <- XMLOps.xmlToJson(xml)
        _ <- SNS.publish(json)
      } yield ()
      result.fold(err => logger.error(err), _ => logger.info(s"Lambda run successfully."))
    case _ =>
      val lastId: String = S3.getLastId
      logger.info(s"Lamda started with lastid=$lastId")
      val result = for {
        body <- HTTP.getXml(lastId)
        xml <- XMLOps.stringToXml(body)
        json <- XMLOps.xmlToJson(xml)
        _ <- SNS.publish(json)
      } yield ()
      result.fold(err => logger.error(err), _ => logger.info(s"Lambda run successfully."))
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
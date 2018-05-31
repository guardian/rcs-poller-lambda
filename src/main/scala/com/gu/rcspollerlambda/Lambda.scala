package com.gu.rcspollerlambda

import com.amazonaws.services.lambda.runtime.Context
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.services.{ HTTP, Logging, XMLOps }

import scala.concurrent.duration._
import scala.concurrent.Await

class LambdaInput() {
  var name: String = _
  def getName(): String = name
  def setName(theName: String): Unit = name = theName
}

object Lambda extends Logging with HTTP with Config {
  /*
   * This is your lambda entry point
   */
  def handler(context: Context): Unit = {
    process()
  }

  def process(): Unit = stage match {
    case "DEV" =>
      val json = XMLOps.xmlToJson(XMLOps.loadXmlFromS3)
      logger.info("Done!")
    case _ => Await.ready(XMLOps.fetchXml, 60.seconds)
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
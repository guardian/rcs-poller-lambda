package com.gu.rcspollerlambda

import com.amazonaws.services.lambda.runtime.Context

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
      logger.info(json.noSpaces)
    case _ => XMLOps.fetchXml
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}
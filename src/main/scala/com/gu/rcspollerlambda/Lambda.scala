package com.gu.rcspollerlambda

import com.amazonaws.services.lambda.runtime.Context

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }
import scala.xml.{ Elem, XML }

/**
 * This is compatible with aws' lambda JSON to POJO conversion.
 * You can test your lambda by sending it the following payload:
 * {"name": "Bob"}
 */
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
    logger.info(s"Starting RCS Poller Lambda")
    process()
  }

  /*
   * I recommend to put your logic outside of the handler
   */
  def process(): Unit = stage match {
    case "DEV" => println(readXml)
    case _ => fetchXml
  }

  def fetchXml = wsClient.url(rcsUrl).get().onComplete {
    case Success(result) => logger.info(result.body)
    case Failure(err) => logger.error(err.getMessage)
  }

  // TODO: Hit RCS endpoint for XML
  def readXml: Elem = XML.loadFile(System.getProperty("user.dir") + "/src/main/scala/com/gu/rcspollerlambda/example.xml")
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}

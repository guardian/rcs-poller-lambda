package com.gu.rcspollerlambda

import com.gu.rcspollerlambda.Lambda.{logger, rcsUrl, wsClient}
import com.gu.rcspollerlambda.models.RightsBatch
import io.circe.Json
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, XML}

object XMLOps {
  def fetchXml = wsClient.url(rcsUrl).withQueryStringParameters(("lastid", "26250821"), ("subscribername", "TEST")).get().onComplete {
    case Success(result) => logger.info(result.body)
    case Failure(err) =>
      logger.error(err.getMessage)
      //TODO: Remove once the RCS endpoint is done
      logger.info("Trying to read from file instead...")
      val json = xmlToJson(readXml)
      SNS.publish(json)
  }

  def xmlToJson(tagsSets: Elem): Json = {
    val rb = RightsBatch(tagsSets)
    logger.info("Extracted rights from XML", rb)
    rb.asJson
  }

  // For DEV only
  def readXml: Elem = {
    val loader = getClass.getClassLoader
    val file = loader.getResource("example.xml").getFile
    logger.info(s"Upload XML from file: $file")
    val xml = XML.loadFile(file)
    logger.info(xml.toString())
    xml
  }
}

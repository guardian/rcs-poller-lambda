package com.gu.rcspollerlambda

import com.gu.rcspollerlambda.Lambda.{ logger, wsClient }
import com.gu.rcspollerlambda.models.RightsBatch
import io.circe.Json
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }
import scala.xml.{ Elem, XML }

object XMLOps extends Config {
  def fetchXml = wsClient.url(rcsUrl).withQueryStringParameters(("lastid", "26250821"), ("subscribername", "TEST")).get().onComplete {
    case Success(result) => logger.info(result.body)
    case Failure(err) =>
      logger.error(s"Error fetching RCS updates: ${err.getMessage}. Trying to read from S3 file instead...")
      //TODO: Remove once the RCS endpoint is done
      val json = xmlToJson(loadXmlFromS3)
      SNS.publish(json)
  }

  def xmlToJson(tagsSets: Elem): Json = {
    val rb = RightsBatch(tagsSets)
    logger.info("Extracted rights from XML", rb)
    rb.asJson
  }

  // For DEV only
  def loadXmlFromS3: Elem = {
    val xmlInputStream = s3Client.getObject("rcs-poller-lambda-config", "example.xml")
    logger.info(s"Loading XML from S3 bucket: ${xmlInputStream.getBucketName}/${xmlInputStream.getKey}")
    val content = xmlInputStream.getObjectContent

    val xml = XML.load(content)
    logger.info(xml.toString())
    xml
  }
}

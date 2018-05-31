package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.Lambda.wsClient
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.RightsBatch
import io.circe.Json
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{ Elem, XML }

object XMLOps extends Config with Logging {
  def fetchXml: Future[Unit] = wsClient.url(rcsUrl).withQueryStringParameters(("lastid", "26400822"), ("subscribername", "TEST")).get().map { result =>
    result.status match {
      case 200 =>
        logger.info(s"Loading XML from response...")
        val xml = XML.load(result.body)
        val json = xmlToJson(Some(xml))
        SNS.publish(json)
      case status => logger.error(s"Error fetching RCS updates. Status: $status; Message: ${result.body}.")
    }
  }

  def xmlToJson(tagsSets: Option[Elem]): Json = {
    logger.info(s"Converting XML to Json...")
    RightsBatch(tagsSets).asJson
  }

  // For DEV only
  def loadXmlFromS3: Option[Elem] = {
    try {
      val xmlInputStream = s3Client.getObject("rcs-poller-lambda-config", "example.xml")
      logger.info(s"Loading XML from S3 bucket: ${xmlInputStream.getBucketName}/${xmlInputStream.getKey}")
      val content = xmlInputStream.getObjectContent

      Some(XML.load(content))
    } catch {
      case e: Throwable =>
        logger.info(s"Error while getting example xml from S3 bucket: $e")
        None
    }
  }
}

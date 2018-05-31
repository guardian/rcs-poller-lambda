package com.gu.rcspollerlambda.services

import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.RightsBatch
import io.circe.Json
import io.circe.syntax._

import scala.xml.{ Elem, XML }

object XMLOps extends Config with Logging {

  def stringToXml(str: String): Either[String, Elem] = {
    logger.info("Loading XML from response body...")
    try { Right(XML.load(str)) } catch { case e: Throwable => Left(s"Error while loading the XML: ${e.getMessage}") }
  }
  def stringToXml(str: S3ObjectInputStream): Either[String, Elem] = try { Right(XML.load(str)) } catch { case e: Throwable => Left(s"Error while loading the XML: ${e.getMessage}") }

  def xmlToJson(tagsSets: Elem): Either[String, Json] = {
    logger.info(s"Converting XML to Json...")
    try { Right(RightsBatch(tagsSets).asJson) } catch { case e: Throwable => Left(s"Error while converting XML to JSON: ${e.getMessage}") }
  }
}

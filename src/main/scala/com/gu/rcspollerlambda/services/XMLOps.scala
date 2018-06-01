package com.gu.rcspollerlambda.services

import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.RightsBatch

import scala.xml.{ Elem, XML }

object XMLOps extends Config with Logging {

  def stringToXml(str: String): Either[String, Elem] = {
    logger.info("Loading XML from response body...")
    try { Right(XML.load(str)) } catch { case e: Throwable => Left(s"Error while loading the XML: ${e.getMessage}") }
  }

  def stringToXml(str: S3ObjectInputStream): Either[String, Elem] = try { Right(XML.load(str)) } catch { case e: Throwable => Left(s"Error while loading the XML: ${e.getMessage}") }

  def xmlToRightsBatch(rcsRightsFeed: Elem): Either[String, RightsBatch] = {
    logger.info(s"Converting XML to RightsBatch...")
    try {
      Right(RightsBatch(rcsRightsFeed))
    } catch {
      case e: Throwable => Left(s"Error while converting XML to a RightsBatch: ${e.getMessage}")
    }
  }
}

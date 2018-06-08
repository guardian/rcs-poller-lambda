package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ ConversionError, LambdaError, RightsBatch, XMLLoadingError }

import scala.xml.{ Elem, XML }

object XMLOps extends Config with Logging {

  def stringToXml(str: String): Either[LambdaError, Elem] = {
    logger.info("Loading XML from response body...")
    try { Right(XML.load(str)) } catch { case e: Throwable => Left(XMLLoadingError(e.getMessage)) }
  }

  def xmlToRightsBatch(rcsRightsFeed: Elem): Either[LambdaError, RightsBatch] = {
    logger.info(s"Converting XML to RightsBatch...")
    try {
      Right(RightsBatch(rcsRightsFeed))
    } catch {
      case e: Throwable => Left(ConversionError(e.getMessage))
    }
  }
}

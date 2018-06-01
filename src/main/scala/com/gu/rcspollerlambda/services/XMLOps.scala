package com.gu.rcspollerlambda.services

import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.gu.rcspollerlambda.config.{ Config, ConversionError, LambdaError, XMLLoadingError }
import com.gu.rcspollerlambda.models.RightsBatch

import scala.xml.{ Elem, XML }

object XMLOps extends Config with Logging {

  def stringToXml(str: String): Either[LambdaError, Elem] = {
    logger.info("Loading XML from response body...")
    try { Right(XML.load(str)) } catch { case e: Throwable => Left(XMLLoadingError(e.getMessage)) }
  }

  def stringToXml(str: S3ObjectInputStream): Either[LambdaError, Elem] =
    try { Right(XML.load(str)) } catch { case e: Throwable => Left(XMLLoadingError(e.getMessage)) }

  def xmlToRightsBatch(rcsRightsFeed: Elem): Either[LambdaError, RightsBatch] = {
    logger.info(s"Converting XML to RightsBatch...")
    try {
      Right(RightsBatch(rcsRightsFeed))
    } catch {
      case e: Throwable => Left(ConversionError(e.getMessage))
    }
  }
}

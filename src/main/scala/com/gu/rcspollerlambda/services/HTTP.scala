package com.gu.rcspollerlambda.services

import java.io.{ PrintWriter, StringWriter }

import com.gu.rcspollerlambda.models.{ LambdaError, MetadataServicePublishError, RCSError }
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import play.api.libs.ws.DefaultBodyWritables._

object HTTP extends Logging {

  def getXml(wsClient: StandaloneAhcWSClient, url: String, lastid: Long, subscriberName: String): Either[LambdaError, String] = {
    logger.info(s"Fetching XML from $url?lastid=$lastid&subscribername=$subscriberName")
    try {
      Await.result(wsClient.url(url).withQueryStringParameters(("lastid", lastid.toString), ("subscribername", subscriberName)).get().map { result =>
        logger.info(s"Status of GET request was ${result.status}")
        result.status match {
          case 200 => Right(result.body)
          case _ => Left(RCSError(s"${result.status}: ${result.body}"))
        }
      }, 5.minutes)
    } catch {
      case e: Throwable =>
        val fullStackTraceWriter = new StringWriter()
        e.printStackTrace(new PrintWriter(fullStackTraceWriter))
        Left(RCSError(e.getClass.getCanonicalName + " / " + fullStackTraceWriter.toString))
    }
  }

  def putJson(wsClient: StandaloneAhcWSClient, url: String, body: String, headers: (String, String)*): Either[LambdaError, String] = {
    logger.debug(s"Putting Json to '$url' with body '$body'")
    try {
      Await.result(
        wsClient.url(url)
          .withHttpHeaders(headers: _*)
          .put(body).map { result =>
            logger.debug(s"Status of PUT request was ${result.status}")
            result.status match {
              case 200 => Right(result.body)
              case _ => Left(MetadataServicePublishError(s"HTTP error ${result.status.toString}", result.body))
            }
          },
        30.seconds)
    } catch {
      case e: Throwable =>
        val fullStackTraceWriter = new StringWriter()
        e.printStackTrace(new PrintWriter(fullStackTraceWriter))
        Left(MetadataServicePublishError(e.getClass.getCanonicalName, fullStackTraceWriter.toString))
    }
  }
}

package com.gu.rcspollerlambda.services

import java.io.{ PrintWriter, StringWriter }

import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.{ LambdaError, MetadataServicePublishError, RCSError }
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import play.api.libs.ws.DefaultBodyWritables._

object HTTP extends Logging {

  def getXml(wsClient: StandaloneAhcWSClient, lastid: Long): Either[LambdaError, String] = Switches.rcsEnabled {
    logger.info(s"Fetching XML from $rcsUrl?lastid=$lastid&subscribername=$subscriberName")
    try {
      Await.result(wsClient.url(rcsUrl).withQueryStringParameters(("lastid", lastid.toString), ("subscribername", subscriberName)).get().map { result =>
        logger.info(s"Status of GET request was ${result.status}")
        result.status match {
          case 200 => Right(result.body)
          case _ => Left(RCSError(result.status + ": " + result.body))
        }
      }, 5.minutes)
    } catch {
      case e: Throwable => handle(e)
    }
  }

  private def handle(e: Throwable): Left[LambdaError, String] = {
    val fullStackTraceWriter = new StringWriter()
    e.printStackTrace(new PrintWriter(fullStackTraceWriter))
    Left(RCSError(e.getClass.getCanonicalName + " / " + fullStackTraceWriter.toString))
  }

  def putJson(wsClient: StandaloneAhcWSClient, url: String, body: String, headers: (String, String)*): Either[LambdaError, String] = {
    logger.info(s"Putting Json to '$url' with body '$body'")
    try {
      Await.result(
        wsClient.url(rcsUrl)
          .withHttpHeaders(headers: _*)
          .put(body).map { result =>
            logger.info(s"Status of PUT request was ${result.status}")
            result.status match {
              case 200 => Right(result.body)
              case _ => Left(MetadataServicePublishError(result.status.toString, result.body))
            }
          },
        30.seconds)
    } catch {
      case e: Throwable => handle(e)
    }
  }
}

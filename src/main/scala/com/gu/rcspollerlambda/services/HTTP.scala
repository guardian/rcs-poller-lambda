package com.gu.rcspollerlambda.services

import java.io.{PrintWriter, StringWriter}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gu.rcspollerlambda.config.{Config, Switches}
import com.gu.rcspollerlambda.models.{LambdaError, RCSError}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HTTP extends Config {
  implicit private val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit private val materializer = ActorMaterializer()
  val wsClient = StandaloneAhcWSClient()

  def getXml(lastid: Long): Either[LambdaError, String] = Switches.rcsEnabled {
    logger.info(s"Fetching XML from $rcsUrl?lastid=$lastid&subscribername=$subscriberName")
    try {
      Await.result(wsClient.url(rcsUrl).withQueryStringParameters(("lastid", lastid.toString), ("subscribername", subscriberName)).get().map { result =>
        logger.info(s"Status of GET request was ${result.status}")
        result.status match {
          case 200 => Right(result.body)
          case _ => Left(RCSError(result.status  + ": " + result.body))
        }
      }, 10.minutes)
    } catch {
      case e: Throwable =>
        wsClient.close()

        val fullStackTraceWriter = new StringWriter()
        e.printStackTrace(new PrintWriter(fullStackTraceWriter))
        Left(RCSError(e.getClass.getCanonicalName + " / " + fullStackTraceWriter.toString))
    }
  }
}

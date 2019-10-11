package com.gu.rcspollerlambda.services

import java.io.{ PrintWriter, StringWriter }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.{ LambdaError, RCSError }
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HTTP extends Logging {
  implicit private val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit private val materializer = ActorMaterializer()

  def getXml(lastid: Long): Either[LambdaError, String] = Switches.rcsEnabled {
    logger.info(s"Fetching XML from $rcsUrl?lastid=$lastid&subscribername=$subscriberName")
    val wsClient = StandaloneAhcWSClient()
    try {
      Await.result(wsClient.url(rcsUrl).withQueryStringParameters(("lastid", lastid.toString), ("subscribername", subscriberName)).get().map { result =>
        logger.info(s"Status of GET request was ${result.status}")
        result.status match {
          case 200 => Right(result.body)
          case _ => Left(RCSError(result.status + ": " + result.body))
        }
      }, 5.minutes)
    } catch {
      case e: Throwable =>
        val fullStackTraceWriter = new StringWriter()
        e.printStackTrace(new PrintWriter(fullStackTraceWriter))
        Left(RCSError(e.getClass.getCanonicalName + " / " + fullStackTraceWriter.toString))
    } finally {
      wsClient.close()
    }
  }
}

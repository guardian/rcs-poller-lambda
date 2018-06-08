package com.gu.rcspollerlambda.services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ LambdaError, RCSError }
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

  def getXml(lastid: String): Either[LambdaError, String] = {
    logger.info(s"Fetching XML from $rcsUrl?lastid=$lastid&subscribername=TEST")
    Await.result(wsClient.url(rcsUrl).withQueryStringParameters(("lastid", lastid), ("subscribername", "TEST")).get().map { result =>
      logger.info(s"Result of GET request was ${result.status}")
      result.status match {
        case 200 => Right(result.body)
        case status => Left(RCSError(status, result.body))
      }
    }, 120.seconds)
  }
}

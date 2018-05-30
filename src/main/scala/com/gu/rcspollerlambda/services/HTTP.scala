package com.gu.rcspollerlambda.services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.StandaloneAhcWSClient

trait HTTP {
  implicit private val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit private val materializer = ActorMaterializer()
  val wsClient = StandaloneAhcWSClient()
}

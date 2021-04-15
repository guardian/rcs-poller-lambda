package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.{ LambdaError, MetadataServicePublishError }
import io.circe.Json
import com.gu.rcspollerlambda.config.Config.{ metadataServiceApiKey, metadataServiceDomain }
import play.api.libs.ws.ahc.StandaloneAhcWSClient

object MetadataService extends Logging {
  def pushRCSUpdates(wsClient: StandaloneAhcWSClient, rcsUpdates: List[(String, Json)]): Either[LambdaError, Unit] = {
    logger.info(s"Sending ${rcsUpdates.length} json RCS update(s) to the Metadata service...")
    Switches.metadataServiceEnabled {
      try {
        Right(rcsUpdates.foreach { case (id, json) => post(wsClient, id, json) })
      } catch {
        case t: Throwable => Left(MetadataServicePublishError("Unable to publish to metadata service", t.toString))
      }
    }
  }

  val headers = "X-Gu-Media-Key" -> metadataServiceApiKey
  def post(wsClient: StandaloneAhcWSClient, id: String, body: Json): Unit = {
    val url = s"https://$metadataServiceDomain/metadata/$id/syndication"
    HTTP.putJson(wsClient, url, body.toString(), headers)
  }

}

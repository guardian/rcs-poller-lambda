package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.{ LambdaError, MetadataServicePublishError }
import io.circe.Json
import com.gu.rcspollerlambda.config.Config.{ metadataServiceApiKey, metadataServiceDomain }
import play.api.libs.ws.ahc.StandaloneAhcWSClient

object MetadataService extends Logging {
  def pushRightsUpdates(wsClient: StandaloneAhcWSClient, rcsUpdates: List[(String, Json)]): Either[LambdaError, String] = {
    logger.info(s"Sending ${rcsUpdates.length} json RCS update(s) to the Metadata service...")
    Switches.metadataServiceEnabled {
      try {
        rcsUpdates
          .filter { case (id, _) =>
            val validId = isGridId(id)
            if (!validId) logger.warn(s"Dropping rights update as $id is not a legitimate grid ID")
            validId
          }
          .map { case (id, json) => post(wsClient, id, json) }
          .collect { case Left(f) => f.message }
          match {
            case Nil => Right(s"${rcsUpdates.length} writes successfully written to Syndication service")
            case errors => Left(MetadataServicePublishError("Error(s) writing to Syndication service", errors.mkString("\n")))
        }
      } catch {
        case t: Throwable => Left(MetadataServicePublishError("Unable to publish to metadata service", t.toString))
      }
    }
  }

  // A grid ID should be 40 characters long and valid hex characters
  def isGridId(potentialId: String): Boolean = {
    // include upper and lowercase to be safe
    val validChars = "0123456789abcdefABCDEF".toSet
    potentialId.length == 40 && potentialId.forall(validChars.contains)
  }

  val securityHeader = "X-Gu-Media-Key" -> metadataServiceApiKey
  val contentHeader = "Content-Type" -> "application/json"
  def post(wsClient: StandaloneAhcWSClient, id: String, body: Json): Either[LambdaError, String] = {
    val url = s"https://$metadataServiceDomain/metadata/$id/syndication"
    HTTP.putJson(wsClient, url, body.toString(), securityHeader, contentHeader)
  }

}

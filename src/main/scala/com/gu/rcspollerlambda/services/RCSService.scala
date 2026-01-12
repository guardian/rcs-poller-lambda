package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Switches
import com.gu.rcspollerlambda.models.LambdaError
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import com.gu.rcspollerlambda.config.Config.rcsUrl
import com.gu.rcspollerlambda.config.Config.subscriberName

object RCSService extends Logging {
  def getXml(
      wsClient: StandaloneAhcWSClient,
      lastId: Long
  ): Either[LambdaError, String] = Switches.rcsEnabled {
    HTTP.getXml(wsClient, rcsUrl, lastId, subscriberName)
  }
}

package com.gu.rcspollerlambda

import akka.actor.ActorSystem
import com.amazonaws.services.lambda.runtime.Context
import com.gu.rcspollerlambda.models.{LambdaError, RightsBatch}
import com.gu.rcspollerlambda.services._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.annotation.nowarn

object Lambda extends Logging {

  implicit private val system: ActorSystem = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }

  /*
   * This is the lambda entry point
   */
  def handler(@nowarn context: Context): Unit = {
    process()
  }

  def process(): Unit = {
    val wsClient = StandaloneAhcWSClient()
    val newLastId: Either[LambdaError, Option[Long]] = try {
      for {
        lastId <- DynamoDB.getLastId
        body <- RCSService.getXml(wsClient, lastId)
        xml <- XMLOps.stringToXml(body)
        rb <- XMLOps.xmlToRightsBatch(xml)
        jsonRightsList <- RightsBatch.toIdParamsWithJsonBodies(rb.rightsUpdates)
        metadataMessage <- MetadataService.pushRightsUpdates(wsClient, jsonRightsList)
      } yield {
        logger.info(metadataMessage)
        rb.lastPosition
      }
    } finally {
      wsClient.close()
    }

    newLastId.fold(
      err => {
        CloudWatch.publishError
        logger.error(err.message)
      },
      {
        case Some(id) =>
          DynamoDB.saveLastId(id)
          CloudWatch.publishOK
          logger.info(s"Lambda run successfully.")
        case None =>
          CloudWatch.publishOK
          logger.warn(s"No new rights tags, lambda will run with the same last id again.")
      })
  }
}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.process()
  }
}

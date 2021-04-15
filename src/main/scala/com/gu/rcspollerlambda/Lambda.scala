package com.gu.rcspollerlambda

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazonaws.services.lambda.runtime.Context
import com.gu.rcspollerlambda.models.{ LambdaError, RightsBatch }
import com.gu.rcspollerlambda.services._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

object Lambda extends Logging {

  implicit private val system: ActorSystem = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  /*
   * This is the lambda entry point
   */
  def handler(context: Context): Unit = {
    process()
  }

  def process(): Unit = {
    val wsClient = StandaloneAhcWSClient()
    val newLastId: Either[LambdaError, Option[Long]] = try {
      for {
        lastId <- DynamoDB.getLastId
        body <- HTTP.getXml(wsClient, lastId)
        xml <- XMLOps.stringToXml(body)
        rb <- XMLOps.xmlToRightsBatch(xml)
        jsonList <- RightsBatch.toIdParamsWithJsonBodies(rb.rightsUpdates)
        json = jsonList.map { case (_, json) => json }
        _ <- Kinesis.publishRCSUpdates(json)
        _ <- MetadataService.pushRCSUpdates(wsClient, jsonList)
      } yield rb.lastPosition
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
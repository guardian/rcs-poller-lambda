package com.gu.rcspollerlambda

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gu.rcspollerlambda.models.RightsBatch
import com.gu.rcspollerlambda.services._
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import java.io.PrintWriter

import scala.io.Source
import scala.util.parsing.json.{ JSONArray, JSONObject }

object BackfillApp extends App with Logging {

  if (args.length == 0) {
    println("dude, i need at least one parameter")
    System.exit(1)
  }
  val rcsUrl = "http://rcslive.dc.gnm.int:8888/ords/grid_rights_feed.changes_since"
  val subscriberName = "AUDIT"
  val batchSize = 1000

  implicit private val system: ActorSystem = ActorSystem()
  implicit private val materializer: ActorMaterializer = ActorMaterializer()
  val wsClient = StandaloneAhcWSClient()

  val inputFile = args(0)
  var lastId = Source.fromFile(inputFile).mkString.trim.toLong
  //  var lastId = 42188494l
  var continue = true
  while (continue) {
    logger.info(s"Fetching $batchSize records starting from $lastId...")
    var batchResult = for {
      body <- HTTP.getXml(wsClient, rcsUrl, lastId, subscriberName, Some(batchSize))
      xml <- XMLOps.stringToXml(body)
      rb <- XMLOps.xmlToRightsBatch(xml)
      jsonRightsList <- RightsBatch.toIdParamsWithJsonBodies(rb.rightsUpdates)
      //    metadataMessage <- MetadataService.pushRightsUpdates(wsClient, jsonRightsList)
    } yield {
      val json = JSONObject(jsonRightsList.map(jr => jr._1 -> jr._2).toMap)
      new PrintWriter(s"rights.$lastId.$batchSize") { write(json.toString()); close }
      logger.info(s"${jsonRightsList.length} records returned")
      (rb.lastPosition, jsonRightsList.length)
    }
    batchResult match {
      case Left(error) =>
        logger.info(error.toString)
        continue = false
      case Right((Some(endOfBatch), fetched)) =>
        lastId = endOfBatch
        if (fetched == 0) {
          logger.info("Stoooooooop!")
          continue = false
        } else {
          new PrintWriter(inputFile) { write(lastId.toString); close }
        }
      case _ =>
        logger.info("what the hell?")
        continue = false
    }
  }
  system.terminate()
}

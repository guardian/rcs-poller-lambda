package com.gu.rcspollerlambda.services

import cats.syntax.either._
import com.gu.rcspollerlambda.config.Config
import com.gu.scanamo.Scanamo

object DynamoDB extends Config {
  def getLastId: Either[String, String] = {
    logger.info(s"Reading lastid from the ${AWS.dynamoTableName} table...")
    Scanamo.scan[String](AWS.dynamoClient)(AWS.dynamoTableName).headOption
      .getOrElse(Right("26400822"))
      .leftMap(dynamoReadError => s"Error while fetching lastid from db: $dynamoReadError")
  }
}
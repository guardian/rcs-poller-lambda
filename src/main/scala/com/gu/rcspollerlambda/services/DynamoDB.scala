package com.gu.rcspollerlambda.services

import cats.syntax.either._
import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ DynamoReadError, LambdaError }
import com.gu.scanamo.Scanamo

object DynamoDB extends Config {
  def getLastId: Either[LambdaError, String] = {
    logger.info(s"Reading lastid from the ${AWS.dynamoTableName} table...")
    Scanamo.scan[String](AWS.dynamoClient)(AWS.dynamoTableName).headOption
      .getOrElse(Right("26400822")) //TODO: Return error instead (temporarily leaving it for testing)
      .leftMap(dynamoReadError => DynamoReadError(dynamoReadError.toString))
  }
}
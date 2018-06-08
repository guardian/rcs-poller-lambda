package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Config
import com.gu.rcspollerlambda.models.{ DynamoReadError, LambdaError }
import com.gu.scanamo.Scanamo

object DynamoDB extends Config {
  case class LastId(id: String)

  def getLastId: Either[LambdaError, String] = {
    logger.info(s"Reading lastid from the ${AWS.dynamoTableName} table...")
    Scanamo.scanWithLimit[LastId](AWS.dynamoClient)(AWS.dynamoTableName, 1).headOption
      .getOrElse(Left(DynamoReadError("No id found in the table.")))
      .fold(dynamoReadError => Left(DynamoReadError(dynamoReadError.toString)), lastId => Right(lastId.id))
  }

  def saveLastId(id: String): Either[LambdaError, Unit] = {
    logger.info(s"Saving the new last id $id in the ${AWS.dynamoTableName} table...")
    Scanamo.put(AWS.dynamoClient)(AWS.dynamoTableName)(LastId(id))
      .getOrElse(Right(()))
      .fold(dynamoReadError => Left(DynamoReadError(dynamoReadError.toString)), _ => Right(()))
  }
}
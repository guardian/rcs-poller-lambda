package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.models.{DynamoReadError, LambdaError}
import com.gu.scanamo.Scanamo

object DynamoDB extends Logging {
  case class LastId(id: String = "lastid", value: Long)

  def getLastId: Either[LambdaError, Long] = {
    logger.info(s"Reading the last id from the ${AWS.dynamoTableName} table...")
    Scanamo.scanWithLimit[LastId](AWS.dynamoClient)(AWS.dynamoTableName, 1).headOption
      .getOrElse(Left(DynamoReadError("No id found in the table.")))
      .fold(dynamoReadError => Left(DynamoReadError(dynamoReadError.toString)), lastId => Right(lastId.value))
  }

  def saveLastId(id: Long): Either[LambdaError, Unit] = {
    logger.info(s"Saving the new last id $id in the ${AWS.dynamoTableName} table...")
    Scanamo.put(AWS.dynamoClient)(AWS.dynamoTableName)(LastId(value = id))
      .getOrElse(Right(()))
      .fold(dynamoReadError => Left(DynamoReadError(dynamoReadError.toString)), _ => Right(()))
  }
}
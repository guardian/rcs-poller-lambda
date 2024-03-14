package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Config._
import com.gu.rcspollerlambda.models.{ DynamoReadError, DynamoWriteError, LambdaError }
import org.scanamo.{ Scanamo, Table }
import org.scanamo.generic.auto._

import scala.util.Try

object DynamoDB extends Logging {
  case class LastId(id: String = "lastid", value: Long)

  val scanamo = Scanamo(AWS.dynamoClient)

  val table = Table[LastId](AWS.dynamoTableName)

  def getLastId: Either[LambdaError, Long] = {
    logger.info(s"Reading the last id from the ${AWS.dynamoTableName} table...")
    scanamo.exec(table.limit(1).scan()).headOption
      .getOrElse(Left(DynamoReadError("No id found in the table.")))
      .fold(dynamoReadError => Left(DynamoReadError(dynamoReadError.toString)), lastId => Right(lastId.value))
  }

  def saveLastId(id: Long): Either[LambdaError, Unit] = {
    logger.info(s"Saving the new last id $id in the ${AWS.dynamoTableName} table...")
    Try {
      scanamo.exec(table.put(LastId(value = id)))
    }.toEither.left.map(error => DynamoWriteError(error.toString))
  }
}
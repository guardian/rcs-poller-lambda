package com.gu.rcspollerlambda.config

sealed trait LambdaError {
  val message: String
}

case class DynamoReadError(err: String) extends LambdaError {
  override val message: String = s"Error while fetching lastid from db: $err"
}

case class S3DownloadError(file: String, err: String) extends LambdaError {
  override val message: String = s"Error while downloading file $file from S3: $err"
}

case class XMLLoadingError(err: String) extends LambdaError {
  override val message: String = s"Error while loading the XML: $err"
}

case class ConversionError(err: String) extends LambdaError {
  override val message: String = s"Conversion error: $err"
}

case class SNSPublishError(id: String, err: String) extends LambdaError {
  override val message: String = s"Error while sending message to SNS for image id $id: $err"
}
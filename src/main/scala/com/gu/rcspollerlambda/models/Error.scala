package com.gu.rcspollerlambda.models

sealed trait LambdaError {
  val message: String
}

case class DynamoReadError(err: String) extends LambdaError {
  override val message: String = s"Error while fetching lastid from db: $err"
}

case class S3DownloadError(file: String, err: String) extends LambdaError {
  override val message: String = s"Error while downloading file $file from S3: $err"
}

case class RCSError(err: String) extends LambdaError {
  override val message: String = s"Error fetching RCS updates: $err"
}

case class XMLLoadingError(err: String) extends LambdaError {
  override val message: String = s"Error while loading the XML: $err"
}

case class ConversionError(err: String) extends LambdaError {
  override val message: String = s"Conversion error: $err"
}

case class MetadataServicePublishError(errorDescription: String, err: String) extends LambdaError {
  override val message: String = s"Error ($errorDescription) while sending message to Metadata Service: $err"
}
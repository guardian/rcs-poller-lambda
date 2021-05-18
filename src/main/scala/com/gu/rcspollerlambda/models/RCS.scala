package com.gu.rcspollerlambda.models

import cats.implicits._
import cats.syntax.either._
import com.gu.rcspollerlambda.services.Logging
import io.circe.generic.semiauto.deriveEncoder
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Encoder, Json, Printer}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

import scala.xml.{Elem, Node}

case class RightsBatch(rightsUpdates: Seq[RCSUpdate], lastPosition: Option[Long])
object RightsBatch extends Logging {
  implicit val encoder: Encoder[RightsBatch] = deriveEncoder[RightsBatch]

  def apply(rcsRightsFeed: Elem): RightsBatch = {
    val tagsSets = rcsRightsFeed \ "tagSet"
    logger.info(s"Fetched ${tagsSets.length} tag sets")

    val rightsUpdates = for (ts <- tagsSets) yield {
      val tagSetId = (ts \ "@tagSetId").headOption.map(_.text).map(_.toLong)

      val rights = for (r <- ts \ "rights" \ "right") yield {
        val rightCode = (r \ "rightCode").text
        val acquired = extractOptBoolean(r, "acquired")
        val properties = for (p <- r \ "properties" \ "property") yield {
          val propertyCode = (p \ "propertyCode").text
          val expiresOn = (p \ "expiresOn").headOption.map { dtNode =>
            new DateTime(dtNode.text)
          }
          val pValue = extractOptString(p, "value")
          Property(propertyCode, expiresOn, pValue)
        }
        Right(rightCode, acquired, properties)
      }

      val id = (ts \ "mediaId").text
      val published = (ts \ "published").headOption.map { dtNode =>
        new DateTime(dtNode.text)
      }

      val suppliers: Seq[Supplier] = for (s <- ts \ "suppliers" \ "supplier") yield {
        val supplierId = extractOptString(s, "supplierId")
        val supplierName = extractOptString(s, "supplierName")
        val prAgreement = extractOptBoolean(s, "prAgreement")
        Supplier(supplierName, supplierId, prAgreement)
      }

      RCSUpdate(tagSetId.get, id, SyndicationRights(published, suppliers, rights))
    }

    val lastPosition = rightsUpdates.lastOption.map(_.tagSetId)

    RightsBatch(rightsUpdates, lastPosition)
  }

  private val THRALL_MESSAGE_TYPE: String = "upsert-rcs-rights"

  def toIdParamsWithJsonBodies(rightsBatch: Seq[RCSUpdate]): Either[LambdaError, List[(String, Json)]] = {
    logger.info(s"Converting Seq[RCSUpdate] to (id, Json) pairs for Metadata service requests...")
    val printer = Printer.noSpaces.copy(dropNullValues = true)
    rightsBatch.map { rcsUpdate =>
      val stringWithNoNulls = printer.pretty(rcsUpdate.data.asJson)
      parse(stringWithNoNulls)
        .map(json => {
          rcsUpdate.id -> Json.obj("data" -> json)
        })
        .leftMap(parsingFailure => ConversionError(parsingFailure.getMessage()))
    }.toList.sequence
  }

  private def nowISO8601 = DateTime.now(DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime())

  private def extractOptString(node: Node, fieldName: String): Option[String] = (node \ fieldName).text match {
    case "" => None
    case value => Some(value)
  }

  private def extractOptBoolean(node: Node, fieldName: String): Option[Boolean] = (node \ fieldName).text match {
    case "" => None
    case value => Some(value == "Y")
  }
}

object DateTimeFormatter {
  private val formatter = ISODateTimeFormat.dateTime()
  implicit val dateTimeEncoder = new Encoder[DateTime] {
    def apply(d: DateTime): Json = {
      val utc = d.withZone(DateTimeZone.UTC)
      formatter.print(utc).asJson
    }
  }
}

case class RCSUpdate(tagSetId: Long, id: String, data: SyndicationRights)
object RCSUpdate {
  implicit val encoder: Encoder[RCSUpdate] = deriveEncoder[RCSUpdate]
}

case class SyndicationRights(published: Option[DateTime], suppliers: Seq[Supplier], rights: Seq[Right], isInferred: Boolean = false)
object SyndicationRights {
  import DateTimeFormatter._
  implicit val encoder: Encoder[SyndicationRights] = deriveEncoder[SyndicationRights]
}

case class Supplier(supplierName: Option[String], supplierId: Option[String], prAgreement: Option[Boolean])
object Supplier {
  implicit val encoder: Encoder[Supplier] = deriveEncoder[Supplier]
}

case class Right(rightCode: String, acquired: Option[Boolean], properties: Seq[Property])
object Right {
  implicit val encoder: Encoder[Right] = deriveEncoder[Right]
}

case class Property(propertyCode: String, expiresOn: Option[DateTime], value: Option[String])
object Property {
  import DateTimeFormatter._
  implicit val encoder: Encoder[Property] = deriveEncoder[Property]
}


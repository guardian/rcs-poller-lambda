package com.gu.rcspollerlambda.models

import com.gu.rcspollerlambda.Lambda.logger
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import io.circe.{ Encoder, Json }
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }

import scala.xml.Elem

case class RightsBatch(rightsUpdates: Seq[RCSUpdate], lastPosition: Option[Long])
object RightsBatch {
  implicit val encoder: Encoder[RightsBatch] = deriveEncoder[RightsBatch]

  def apply(rcsRightsFeed: Elem): RightsBatch = {
    val tagsSets = rcsRightsFeed \ "tagSet"
    logger.info(s"Fetched ${tagsSets.length} tag sets")

    val rightsUpdates = for (ts <- tagsSets) yield {
      val tagSetId = (ts \ "@tagSetId").headOption.map(_.text).map(_.toLong)

      val rights = for (r <- ts \ "rights" \ "right") yield {
        val code = (r \ "rightCode").text
        val acquired = (r \ "acquired").text == "Y"
        val properties = for (p <- r \ "properties" \ "property") yield {
          val pCode = (p \ "propertyCode").text
          val pExpiresOn = (p \ "expiresOn").headOption.map { dtNode =>
            new DateTime(dtNode.text)
          }
          val pValue = (p \ "value").text
          val pValueOpt = if (pValue == "") None else Some(pValue)
          Property(pCode, pExpiresOn, pValueOpt)
        }
        RightAcquisition(code, acquired, if (properties.isEmpty) None else Some(properties))
      }

      val id = (ts \ "mediaId").text
      val supplierName = (ts \ "supplierName").text
      val supplierId = (ts \ "supplierID").text
      val prAgreement = (ts \ "prAgreement").text == "Y"

      RCSUpdate(tagSetId.get, id, supplierName, supplierId, prAgreement, rights)
    }

    val lastPosition = rightsUpdates.lastOption.map(_.tagSetId)

    RightsBatch(rightsUpdates, lastPosition)
  }
}

case class RCSUpdate(tagSetId: Long, id: String, supplierName: String, supplierId: String, prAgreement: Boolean, contentRights: Seq[RightAcquisition])
object RCSUpdate {
  implicit val encoder: Encoder[RCSUpdate] = deriveEncoder[RCSUpdate]
}

case class RightAcquisition(code: String, acquired: Boolean, properties: Option[Seq[Property]])
object RightAcquisition {
  implicit val encoder: Encoder[RightAcquisition] = deriveEncoder[RightAcquisition]
}

case class Property(code: String, expiresOn: Option[DateTime], value: Option[String])
object Property {
  private val formatter = ISODateTimeFormat.dateTime()

  implicit val dateTimeEncoder = new Encoder[DateTime] {
    def apply(d: DateTime): Json = {
      val utc = d.withZone(DateTimeZone.UTC)
      formatter.print(utc).asJson
    }
  }

  implicit val encoder: Encoder[Property] = deriveEncoder[Property]
}

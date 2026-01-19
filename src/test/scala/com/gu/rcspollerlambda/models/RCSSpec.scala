package com.gu.rcspollerlambda.models

import org.joda.time.DateTime
import org.scalactic.Equality
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues

import scala.xml.XML

class RCSSpec extends AnyFlatSpec with Matchers with OptionValues {
  val fixture = """<?xml version="1.0"?>
                  |<rcsRightsFeed>
                  |  <tagSet tagSetId="26884766">
                  |    <contentType>Photograph</contentType>
                  |    <mediaId>ecfc474ec82a6882103b68c0e272a6e92e6775f8</mediaId>
                  |    <published>2019-01-27T00:10:00</published>
                  |    <suppliers>
                  |      <supplier>
                  |        <supplierId>GNL000565</supplierId>
                  |        <supplierName>Getty Images International</supplierName>
                  |        <prAgreement>Y</prAgreement>
                  |      </supplier>
                  |    </suppliers>
                  |    <rights>
                  |      <right>
                  |        <rightCode>LICENSINGNONSUBSALES</rightCode>
                  |        <acquired>Y</acquired>
                  |        <properties>
                  |          <property>
                  |            <propertyCode>TERM</propertyCode>
                  |            <expiresOn>2018-07-31</expiresOn>
                  |            <value>Contract term</value>
                  |          </property>
                  |          <property>
                  |            <propertyCode>EXERCISABLEPERIOD</propertyCode>
                  |            <expiresOn>2018-07-31</expiresOn>
                  |            <value>cats album</value>
                  |          </property>
                  |          <property>
                  |            <propertyCode>EXCLUSIVITYPERIOD</propertyCode>
                  |            <expiresOn>2018-05-30T13:59:19</expiresOn>
                  |            <value>0 Year(s)</value>
                  |          </property>
                  |          <property>
                  |            <propertyCode>PRIORCONTRIBUTORAPPROVAL</propertyCode>
                  |            <value>N</value>
                  |          </property>
                  |          <property>
                  |            <propertyCode>TERRITORY</propertyCode>
                  |            <value>World all</value>
                  |          </property>
                  |          <property>
                  |            <propertyCode>PRIORAPPROVALRE</propertyCode>
                  |          </property>
                  |          <property>
                  |            <propertyCode>TERRITORYEXCLUSIVITY</propertyCode>
                  |            <value>None</value>
                  |          </property>
                  |          <property>
                  |            <propertyCode>TERRITORYPRIORAPPROVAL</propertyCode>
                  |          </property>
                  |        </properties>
                  |      </right>
                  |    </rights>
                  |  </tagSet>
                  |</rcsRightsFeed>""".stripMargin

  it should "parse message into RightsBatch" in {
    val feed = XML.loadString(fixture)
    val rightsBatch = RightsBatch(feed)

    rightsBatch.rightsUpdates must have size 1
    rightsBatch.lastPosition.value must equal(26884766)

    val rightUpdate = rightsBatch.rightsUpdates.head
    rightUpdate.tagSetId must equal(26884766)
    rightUpdate.id must equal("ecfc474ec82a6882103b68c0e272a6e92e6775f8")

    val syndicationData = rightUpdate.data
    syndicationData.published.value must equal(
      DateTime.parse("2019-01-27T00:10:00.000Z")
    )

    syndicationData.suppliers must have size 1
    val supplier = syndicationData.suppliers.head
    supplier.supplierId.value must equal("GNL000565")
    supplier.prAgreement.value must be(true)

    syndicationData.rights must have size 1
    val right = syndicationData.rights.head
    right.rightCode must be("LICENSINGNONSUBSALES")
    right.acquired.value must be(true)
    right.properties must have size 8

    val rightProperty = right.properties.head
    rightProperty.propertyCode must be("TERM")
    rightProperty.expiresOn.value must equal(
      DateTime.parse("2018-07-31")
    )
  }

  implicit val jodaEq: Equality[DateTime] =
    new Equality[DateTime] {
      def areEqual(a: DateTime, b: Any): Boolean = b match {
        case dt: DateTime => a.isEqual(dt)
        case _            => false
      }
    }
}

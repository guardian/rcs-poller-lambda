package com.gu.rcspollerlambda.models

import org.scalatest.{ FunSpec, Matchers }
import play.api.libs.json.Json
import org.joda.time.{ DateTime, DateTimeZone }

class RCSTest extends FunSpec with Matchers {

  def cleanJsonAndRemoveDates(s: String) =
    s.split("\n").map(_.trim).mkString("")
      .replaceAll("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z", "date")

  describe("serialisation") {
    it("should correctly serialise a json message") {
      val r = List(RCSUpdate(0l, "id", SyndicationRights(None, Nil, Nil)))
      val rJson = RightsBatch.toJsonMessage(r)
      rJson.isRight should be(true)
      val rJsonContentList = rJson.right.get
      rJsonContentList.length should be(1)
      val rJsonContent = rJsonContentList.head
      cleanJsonAndRemoveDates(rJsonContent.toString()) should be(cleanJsonAndRemoveDates(
        """
        |{
        |     "subject" : "upsert-rcs-rights",
        |     "id" : "id",
        |     "syndicationRights" : {
        |      "suppliers" : [
        |       ],
        |       "rights" : [
        |       ],
        |      "isInferred" : false
        |     },
        |     "lastModified" : "2021-01-27T16:45:55.498Z"
        |}
        |""".stripMargin))
    }
  }
}

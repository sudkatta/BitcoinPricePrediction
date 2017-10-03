package controllers

import models.{PredictionValues, PricePoint}
import play.api.libs.json.Reads._
import play.api.libs.ws._
import play.api.test.{PlaySpecification, WithServer}
import utils.Helpers


/**
  * Created by skatta on 10/3/2017 AD.
  */
class ApplicationIntegrationSpec extends PlaySpecification {

  val Localhost = "http://localhost:"

  "Application" should {

    "be reachable" in new WithServer {
      val response = await(WS.url(Localhost + port).get())
      response.status must equalTo(OK)
      response.body must contain("{\"status\":\"OK\"}")
    }
  }

  "Past prices api" should {

    "return past prices given a date range" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices?range=09/05/2017-10/05/2017").get())
      response.status must equalTo(OK)
      val pricePoints = (response.json \ "data").as[Seq[PricePoint]]
      pricePoints.size must equalTo(2)
    }

    "return past prices for range = lastweek" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices?range=lastweek").get())
      response.status must equalTo(OK)
      val pricePoints = (response.json \ "data").as[Seq[PricePoint]]
      pricePoints.size must equalTo(7)
    }

    "return past prices for range = lastmonth" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices?range=lastmonth").get())
      response.status must equalTo(OK)
      val pricePoints = (response.json \ "data").as[List[PricePoint]]

      pricePoints.size must equalTo(Helpers.getLastMonthCount())
    }

    "return Invalid entry for range = dummy" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices?range=dummy").get())
      response.status must equalTo(BAD_REQUEST)
    }
    "return Invalid entry for range not mentioned" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices").get())
      response.status must equalTo(BAD_REQUEST)
    }
    "return Invalid entry for range from > to" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices?range=09/06/2017-10/05/2017").get())
      response.status must equalTo(BAD_REQUEST)
    }
  }

  "Moving average api" should {

    "return moving average given a from,to, window" in new WithServer {
      val response = await(WS.url(Localhost + port + "/movingAvg?from=09/05/2017&to=10/05/2017&window=10").get())
      response.status must equalTo(OK)
      val pricePoints = (response.json \ "data").as[List[PricePoint]]
      pricePoints.size must equalTo(2)
    }
    "return Invalid entry for from > to from=09/05/2017&to=10/05/2017&window=10" in new WithServer {
      val response = await(WS.url(Localhost + port + "/movingAvg?from=09/06/2017&to=10/05/2017&window=10").get())
      response.status must equalTo(BAD_REQUEST)
    }
    "return Invalid entry for missing keys" in new WithServer {
      val response = await(WS.url(Localhost + port + "/movingAvg").get())
      response.status must equalTo(BAD_REQUEST)
    }
  }

  "Price Prediction api" should {

    "return next 15 days data" in new WithServer {
      val response = await(WS.url(Localhost + port + "/forecast").get())
      response.status must equalTo(OK)
      val pricePoints = (response.json \ "prediction").as[List[PredictionValues]]
      pricePoints.size must equalTo(15)
    }

  }

}

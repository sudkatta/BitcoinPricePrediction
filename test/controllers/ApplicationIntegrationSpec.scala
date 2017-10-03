package controllers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

import akka.util.Timeout
import models.DataLoader.PricePoint
import models.SparkEngine.PredictionValues
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.test.{PlaySpecification, WithServer}
import utils.Helpers



/**
  * Created by skatta on 10/3/2017 AD.
  */
class ApplicationIntegrationSpec extends PlaySpecification {

  implicit val pricePointReads:Reads[PricePoint] = (
          (JsPath \ "price").read[Double] and
            (JsPath \ "date").read[Date]
          )(PricePoint.apply _)
  implicit val DefaultDateReads = dateReads("dd/MM/yyyy")



  val Localhost = "http://localhost:"
  implicit val timeout:Timeout = Timeout(10000, TimeUnit.MILLISECONDS)
  "Application" should {

    "be reachable" in new WithServer {
      val response = await(WS.url(Localhost + port + "/").get())

      response.status must equalTo(OK)
      response.body must contain("{\"status\":\"OK\"}")
    }
  }

  "Past prices api" should {

    "return past prices given a date range" in new WithServer {
      val response1 = await(WS.url(Localhost + port + "/pastPrices?range=09/05/2017-10/05/2017").get())
      response1.status must equalTo(OK)
      val pricePoints = (response1.json \ "data").as[List[PricePoint]]
      pricePoints.size must equalTo(2)
    }

    "return past prices for range = lastweek" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices?range=lastweek").get())
      response.status must equalTo(OK)
      val pricePoints = (response.json \ "data").as[List[PricePoint]]

      pricePoints.size must equalTo(7)
    }

    "return past prices for range = lastmonth" in new WithServer {
      val response = await(WS.url(Localhost + port + "/pastPrices?range=lastmonth").get())
      response.status must equalTo(OK)
      println(response.body)
      val pricePoints = (response.json \ "data").as[List[PricePoint]]

      pricePoints.size must equalTo(Helpers.getLastMonthCount)
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
      val pricePoints = (response.json \ "data").as[List[PredictionValues]]
      pricePoints.size must equalTo(15)
    }

  }

}

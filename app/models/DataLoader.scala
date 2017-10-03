package models

import java.util.{Calendar, Date}

import org.apache.spark.mllib.linalg._
import play.api.libs.json._

import scala.util.parsing.json._
import play.api.libs.json.Writes.dateWrites
import utils.Helpers.dateFormatter

/**
  * Created by Sudheer on 01/10/17.
  */
case class PricePoint(price: Double, date: Date)

object PricePoint {

  implicit val personLastSeen = new Reads[PricePoint] {
    def reads(js: JsValue): JsResult[PricePoint] = {
      JsSuccess(PricePoint(
        (js \ "price").as[Double],
        dateFormatter.parse((js \ "date").as[String])))
    }
  }
  implicit val customDateWrites: Writes[Date] = dateWrites("dd/MM/yyyy")
  implicit val writes = Json.writes[PricePoint]

}

object DataLoader {

  def vectors = pricePoints.map(x => Vectors.dense(x.date.getTime.toDouble))

  var minDateOfDataSet: Option[Date] = None
  private var pricePoints: List[PricePoint] = List.empty

  def getPricePoints: List[PricePoint] = {
    if (pricePoints.isEmpty) refreshData()
    pricePoints
  }

  def refreshData(): Unit = {

    minDateOfDataSet = Some(Calendar.getInstance().getTime)
    val url = "https://www.coinbase.com/api/v2/prices/BTC-USD/historic?period=year"
    val result = scala.io.Source.fromURL(url).mkString

    val parsedData = JSON.parseFull(result).get.asInstanceOf[Map[String, Any]]("data")

    val parsedPrices = parsedData.asInstanceOf[Map[String, Any]]("prices").asInstanceOf[List[Map[String, String]]]

    val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    pricePoints = parsedPrices.map(x => {
      val date = format.parse(x("time"))
      if (date.compareTo(minDateOfDataSet.get) < 0) minDateOfDataSet = Some(date)
      PricePoint(x("price").toDouble, date)
    })
  }


  def getPastData(from: Date, to: Date): List[PricePoint] = {
    if (pricePoints.isEmpty) refreshData()
    pricePoints.filter(p => p.date.compareTo(to) <= 0 && p.date.compareTo(from) >= 0)
  }

}


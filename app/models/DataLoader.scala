package models

import java.util.Date

import org.apache.spark.mllib.linalg._
import play.api.libs.json._

import scala.util.parsing.json._

/**
  * Created by Sudheer on 01/10/17.
  */

object DataLoader {


  case class PricePoint(price: Double, timestamp: Date)


  implicit val writes = Json.writes[PricePoint]
  implicit val reads = Json.reads[PricePoint]

  def vectors = pricePoints.map(x => Vectors.dense(x.timestamp.getTime.toDouble))

  var pricePoints: List[PricePoint] = List.empty

  refreshData()

  def refreshData(): Unit = {

    val url = "https://www.coinbase.com/api/v2/prices/BTC-USD/historic?period=year"
    val result = scala.io.Source.fromURL(url).mkString

    val parsedData = JSON.parseFull(result).get.asInstanceOf[Map[String,Any]]("data")

    val parsedPrices = parsedData.asInstanceOf[Map[String,Any]]("prices").asInstanceOf[List[Map[String, String]]]

    val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    pricePoints = parsedPrices.map(x =>
      PricePoint(x("price").toDouble, format.parse(x("time")))
    )

  }


  def getPastData(to:Date, from: Date):List[PricePoint] = {
    if(pricePoints.isEmpty) refreshData()
    pricePoints.filter(p => p.timestamp.compareTo(to) >= 0 && p.timestamp.compareTo(from) >= 0)
  }

  def getMovingAvg(to:Date, from: Date, period:Int):List[PricePoint] = {
    if(pricePoints.isEmpty) refreshData()
    pricePoints.filter(p => p.timestamp.before(to) && p.timestamp.after(from))
  }

}


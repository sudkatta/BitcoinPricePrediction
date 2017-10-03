package models

import java.util.{Calendar, Date}

import org.apache.spark.mllib.linalg._
import play.api.libs.json._

import scala.util.parsing.json._
import play.api.libs.json.Writes.dateWrites // do not import everything here, especially DefaultDateWrites


/**
  * Created by Sudheer on 01/10/17.
  */

object DataLoader {


  case class PricePoint(price: Double, date: Date)
  implicit val customDateWrites: Writes[java.util.Date] = dateWrites("yyyy-MM-dd")
  implicit val writes = Json.writes[PricePoint]


  def vectors = pricePoints.map(x => Vectors.dense(x.date.getTime.toDouble))
  var minDateOfDataSet:Option[Date] = None
  var pricePoints: List[PricePoint] = List.empty

  refreshData()

  def refreshData(): Unit = {

    minDateOfDataSet = Some(Calendar.getInstance().getTime)
    val url = "https://www.coinbase.com/api/v2/prices/BTC-USD/historic?period=year"
    val result = scala.io.Source.fromURL(url).mkString

    val parsedData = JSON.parseFull(result).get.asInstanceOf[Map[String,Any]]("data")

    val parsedPrices = parsedData.asInstanceOf[Map[String,Any]]("prices").asInstanceOf[List[Map[String, String]]]

    val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    pricePoints = parsedPrices.map(x =>{
      val date = format.parse(x("time"))
      if(date.compareTo(minDateOfDataSet.get) < 0) minDateOfDataSet = Some(date)
      PricePoint(x("price").toDouble, date)
    }

    )

  }


  def getPastData(from: Date, to:Date):List[PricePoint] = {
    if(pricePoints.isEmpty) refreshData()
    pricePoints.filter(p => p.date.compareTo(to) <= 0 && p.date.compareTo(from) >= 0)
  }

}


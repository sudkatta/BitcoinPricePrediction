package models

import utils.SparkEngine.spark
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.joda.time._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import utils.Helpers._
import play.api.libs.functional.syntax._


/**
  * Created by Sudheer on 01/10/17.
  */

case class PredictionValues(price: Double, date: String)
object PredictionValues {
  implicit val predictionValuesReads:Reads[PredictionValues] = (
    (JsPath \ "price").read[Double] and
      (JsPath \ "date").read[String]
    )(PredictionValues.apply _)
  implicit val writesPredictionValues: Writes[PredictionValues] = Json.writes[PredictionValues]
}

object PricePrediction {

  def getPricePrediction: List[PredictionValues] = {
    val now = DateTime.now

    val features = (0 until 15).map(now.plusDays(_)).toList.map(date =>{
      val days = new Period(DataLoader.minDateOfDataSet.get.getTime, date.getMillis, PeriodType.days()).getDays.toDouble
      (Vectors.dense(Array(days)),1)
    })
    val featuresDf = spark.createDataFrame(features).toDF("features","dummy")

    import spark.implicits._
    val fullPredictions = PredictionModels.predict(featuresDf).map(r => {
      val days = r(0).asInstanceOf[Vector](0).toInt
      val date = dateAddDays(DataLoader.minDateOfDataSet.get, days)
      PredictionValues(round(r(1).asInstanceOf[Double]),date.toString)
    }).collectAsList().toArray.toList.asInstanceOf[List[PredictionValues]]
    println(fullPredictions)
    fullPredictions
  }


}


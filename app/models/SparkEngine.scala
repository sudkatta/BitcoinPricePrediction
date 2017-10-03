package models

import java.util.Date

import models.DataLoader.PricePoint
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.{Transformer, regression}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.rdd.RDDFunctions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import utils.Helpers._
import play.api.libs.functional.syntax._




/**
  * Created by Sudheer on 01/10/17.
  */

object SparkEngine {
  private val spConf = new SparkConf().setAppName("Bitcoin price prediction").setMaster("local[*]")
  private val sc = SparkContext.getOrCreate(spConf)
  private val spark = SparkSession
    .builder
    .appName(s"DecisionTreeExample")
    .getOrCreate()

  private var model_prices:Option[regression.LinearRegressionModel] = None

  def train: Unit = {
    if(DataLoader.pricePoints.isEmpty) DataLoader.refreshData()

    val seqPricePoints = DataLoader.pricePoints.map(record => {
      val days = new Period(DataLoader.minDateOfDataSet.get.getTime, record.date.getTime, PeriodType.days()).getDays.toDouble
      (record.price, Vectors.dense(Array(days)))
    })

    val pricePointsDf = spark.createDataFrame(seqPricePoints).toDF("label", "features")
    val splitData = pricePointsDf.randomSplit(Array(0.8,0.2))
    val training = splitData(0).cache()
    val testing = splitData(1).cache()

    val numIterations = 100
    val stepSize = 0.0001

    val algorithm = new LinearRegression()
      .setFeaturesCol("features")
      .setLabelCol("label")
      .setMaxIter(numIterations)
      .setTol(stepSize)

    model_prices = Some(algorithm.fit(training))

    evaluateRegressionModel(model_prices.get, testing, "label")

  }


  def getMovingAverageFromRange(from: Date, to: Date, window: Int):List[PricePoint] ={
    val modifiedFromDate = dateSubtractDays(from, window-1)
    val listOfPricePoints = DataLoader.getPastData(modifiedFromDate, to)
    getMovingAverageFromList(listOfPricePoints, window)
  }

  private def getMovingAverageFromList(input:List[PricePoint], window: Int):List[PricePoint] ={
    val sortedPricePointsInput = input.sortWith((x, y)=> x.date.after(y.date))
    val sortedPricePoints = sc.parallelize(sortedPricePointsInput)
    val listOfPrices = sortedPricePoints.sliding(window).map(curSlice => PricePoint(round(curSlice.map(_.price).sum/curSlice.size), curSlice.map(_.date).max)).collect()
    listOfPrices.sortWith((x, y)=> x.date.after(y.date)).toList
  }

  private def evaluateRegressionModel(model: Transformer,data: DataFrame,labelColName: String): Unit = {
    val fullPredictions = model.transform(data).cache()
    val predictions = fullPredictions.select("prediction").rdd.map(_.getDouble(0))
    val labels = fullPredictions.select(labelColName).rdd.map(_.getDouble(0))
    val RMSE = new RegressionMetrics(predictions.zip(labels)).rootMeanSquaredError
    println(s"  Root mean squared error (RMSE): $RMSE")
  }

  def getPricePrediction: List[PredictionValues] = {
    val now = DateTime.now
    if (model_prices.isEmpty) train

    val features = (0 until 15).map(now.plusDays(_)).toList.map(date =>{
      val days = new Period(DataLoader.minDateOfDataSet.get.getTime, date.getMillis, PeriodType.days()).getDays.toDouble
      (Vectors.dense(Array(days)),1)
    })
    val featuresDf = spark.createDataFrame(features).toDF("features","dummy")
    import spark.implicits._
    val fullPredictions = model_prices.get.transform(featuresDf).select("features", "prediction").map(r => {
      val days = r(0).asInstanceOf[Vector](0).toInt
      val date = dateAddDays(DataLoader.minDateOfDataSet.get, days)
      PredictionValues(round(r(1).asInstanceOf[Double]),date.toString)
    }).collectAsList().toArray.toList.asInstanceOf[List[PredictionValues]]
    println(fullPredictions)
    fullPredictions
  }

  case class PredictionValues(price: Double, date: String)
  implicit val writesPredictionValues:Writes[PredictionValues] = Json.writes[PredictionValues]

}


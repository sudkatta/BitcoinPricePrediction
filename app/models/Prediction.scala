package models

import java.io.ByteArrayOutputStream
import java.util.{Calendar, Date}

import models.DataLoader.PricePoint
import models.Prediction.model_prices
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionModel, LinearRegressionWithSGD}
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.DateTimeZone
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import play.api.libs.json._

import scala.util.parsing.json._

/**
  * Created by Sudheer on 01/10/17.
  */

object Prediction {
  val spConf = new SparkConf().setAppName("Bitcoin price prediction").setMaster("local[*]")
  var spCtx = SparkContext.getOrCreate(spConf)
//  val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  def sc = spCtx

  case class Status(time:String, message:String)
  implicit val statusWrites = Json.writes[Status]
  implicit val statusReads = Json.reads[Status]


  var log = List(Status(Calendar.getInstance().getTime().toString, "Spark is initialized."))
  def clearLog = str.reset()

  var str = new ByteArrayOutputStream()
  Console.setOut(str)

  DataLoader.refreshData()



  var model_prices:LinearRegressionModel = _

  def getCorrelations = model_prices.weights.toString()

  var max_time = 0.0
  var max_price = 0.0


  def train(saveModel:Boolean): Unit = {

    val data_health = DataLoader.pricePoints.map(record => {
      val norm_time = record.timestamp.getTime.toDouble
//      val norm_price = (record.price - max_price)/max_price
      LabeledPoint(record.price, Vectors.dense(norm_time))
    })

    val rdd_health = sc.makeRDD(data_health)
    println(rdd_health.count())

    val splits_health = rdd_health.randomSplit(Array(0.8, 0.2))

    println(splits_health(0).count())
    val training_health = splits_health(0).cache()


    val numIterations = 100
    val stepSize = 0.00000001
    model_prices = LinearRegressionWithSGD.train(training_health, numIterations, stepSize)

//    val algorithm_health = new LinearRegressionWithSGD()
//    algorithm_health.setIntercept(true)
//    model_prices = algorithm_health.run(training_health)

  }

  def getPricePrediction(date: String) = {
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val timestamp = format.parse(date)
    println(timestamp)
    val norm_time = timestamp.getTime.toDouble

    val v:Vector = new DenseVector(Array(norm_time))
    val result = model_prices.predict(v)
    println(result)
    result
  }

}


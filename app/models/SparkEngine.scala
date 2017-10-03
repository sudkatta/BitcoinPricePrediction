package models

import java.io.ByteArrayOutputStream
import java.util.{Calendar, Date}

import models.DataLoader.PricePoint
import models.SparkEngine.model_prices
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionModel, LinearRegressionWithSGD}
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time._
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import play.api.libs.json._

import scala.util.parsing.json._
import org.apache.spark.mllib.rdd.RDDFunctions._
import utils.Helpers._

/**
  * Created by Sudheer on 01/10/17.
  */

object SparkEngine {
  val spConf = new SparkConf().setAppName("Bitcoin price prediction").setMaster("local[*]")
  val jars = Array("/path/to/spark-solr-2.1.0-shaded.jar")
  spConf.setJars(jars)
  val sc = SparkContext.getOrCreate(spConf)
  var model_prices:Option[LinearRegressionModel] = None

  def train: Unit = {

    val labeledPoints = DataLoader.pricePoints.map(record => {
      val days = new Period(DataLoader.minDateOfDataSet.get.getTime, record.date.getTime, PeriodType.days()).getDays.toDouble
      LabeledPoint(record.price, Vectors.dense(days))
    })

//    println(data_health)

    val rddPricePoints = sc.parallelize(labeledPoints)
    println(rddPricePoints.count())

    val splitsPricePoints = rddPricePoints.randomSplit(Array(0.8, 0.2))

    println(splitsPricePoints(0).count())
    val training = splitsPricePoints(0).cache()
    val test = splitsPricePoints(1).cache()


    val numIterations = 100
    val stepSize = 0.0001
//    model_prices = Some(LinearRegressionWithSGD.train(training, numIterations, stepSize))

    val algorithm = new LinearRegressionWithSGD()
    algorithm.setIntercept(true)
    algorithm.optimizer.setNumIterations(numIterations).setStepSize(stepSize)

    model_prices = Some(algorithm.run(training))

    val prediction = test.map(x => model_prices.get.predict(x.features))
    println(prediction.collect())
//    val predictionAndLabel = prediction zip test.map(_.label)
//
//    println(predictionAndLabel.collect())
//    predictionAndLabel.foreach((result) => println("predicted label "+ result._1.toString + " actual label: "+ result._2.toString))

    //    val loss = predictionAndLabel.map { case (p, l) =>
//      val err = p - l
//      err * err
//    }.reduce(_ + _)
//
//    val rmse = math.sqrt(loss / test.count())
//    println("error: "+rmse.toString)


    //    val algorithm_health = new LinearRegressionWithSGD()
//    algorithm_health.setIntercept(true)
//    model_prices = Some(algorithm_health.run(training_health))

  }


  def getMovingAverageFromRange(from: Date, to: Date, window: Int):List[PricePoint] ={
    val modifiedFromDate = dateSubtractDays(from, window-1)
    val listOfPricePoints = DataLoader.getPastData(modifiedFromDate, to)
    getMovingAverageFromList(listOfPricePoints, window)
  }

  def getMovingAverageFromList(input:List[PricePoint], window: Int):List[PricePoint] ={
    val sortedPricePointsInput = input.sortWith((x, y)=> x.date.after(y.date))
    val sortedPricePoints = sc.parallelize(sortedPricePointsInput)
    val listOfPrices = sortedPricePoints.sliding(window).map(curSlice => PricePoint(round(curSlice.map(_.price).sum/curSlice.size), curSlice.map(_.date).max)).collect()
    listOfPrices.sortWith((x, y)=> x.date.after(y.date)).toList
  }


  def getPricePrediction:List[PricePoint] = {
    val now = DateTime.now
    if (model_prices.isEmpty) train
    println(model_prices.get.intercept)
    println(model_prices.get.weights)
    println(model_prices.get.toPMML())

    val prediction = (0 until 15).map(now.plusDays(_)).map(date =>{
      val days = new Period(DataLoader.minDateOfDataSet.get.getTime, date.getMillis, PeriodType.days()).getDays.toDouble
//      println(days)
      val v:Vector = new DenseVector(Array(days))
      val price = model_prices.get.predict(v)
      PricePoint(price, date.toDate)
    }
    )
//    println(prediction)
    prediction.toList
  }

}


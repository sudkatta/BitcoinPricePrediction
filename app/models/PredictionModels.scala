package models

import utils.SparkEngine.spark
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.{Transformer, regression}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.sql.DataFrame
import org.joda.time._


/**
  * Created by Sudheer on 01/10/17.
  */

object PredictionModels {


  private var model_prices:Option[regression.LinearRegressionModel] = None

  def train: Unit = {

    val seqPricePoints = DataLoader.getPricePoints.map(record => {
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

  private def evaluateRegressionModel(model: Transformer,data: DataFrame,labelColName: String): Unit = {
    val fullPredictions = model.transform(data).cache()
    val predictions = fullPredictions.select("prediction").rdd.map(_.getDouble(0))
    val labels = fullPredictions.select(labelColName).rdd.map(_.getDouble(0))
    val RMSE = new RegressionMetrics(predictions.zip(labels)).rootMeanSquaredError
    println(s"  Root mean squared error (RMSE): $RMSE")
  }

  def predict(featuresDf:DataFrame):DataFrame = {
    if (model_prices.isEmpty) train
    model_prices.get.transform(featuresDf).select("features", "prediction")
  }
}


package utils

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}




/**
  * Created by Sudheer on 01/10/17.
  */

object SparkEngine {
  private val spConf = new SparkConf().setAppName("Bitcoin price prediction").setMaster("local[*]")
  val sc = SparkContext.getOrCreate(spConf)
  val spark = SparkSession
    .builder
    .appName(s"Bitcoin price prediction")
    .getOrCreate()

}


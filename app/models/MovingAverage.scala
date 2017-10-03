package models

import java.util.Date

import utils.SparkEngine.sc
import org.apache.spark.mllib.rdd.RDDFunctions._
import utils.Helpers._


/**
  * Created by Sudheer on 01/10/17.
  */

object MovingAverage {

  def getMovingAverageFromRange(from: Date, to: Date, window: Int):List[PricePoint] ={
    val modifiedFromDate = dateAddDays(from, -window+1)
    val listOfPricePoints = DataLoader.getPastData(modifiedFromDate, to)
    getMovingAverageFromList(listOfPricePoints, window)
  }

  private[models] def getMovingAverageFromList(input:List[PricePoint], window: Int):List[PricePoint] ={
    val sortedPricePointsInput = input.sortWith((x, y)=> x.date.after(y.date))
    val sortedPricePoints = sc.parallelize(sortedPricePointsInput)
    val listOfPrices = sortedPricePoints.sliding(window).map(curSlice => PricePoint(round(curSlice.map(_.price).sum/curSlice.size), curSlice.map(_.date).max)).collect()
    listOfPrices.sortWith((x, y)=> x.date.after(y.date)).toList
  }

}


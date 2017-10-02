package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import models.{DataLoader, Prediction}
import play.api.libs.json._
import play.api.mvc._

import scala.util.Try

/**
  * Created by Sudheer on 01/10/17.
  */

object Application extends Controller {

  def getLastWeekDateRange(): (Date, Date) ={
    val calendar = Calendar.getInstance()
    val i = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek
    calendar.add(Calendar.DATE, -i - 7)
    val start = calendar.getTime
    calendar.add(Calendar.DATE, 6)
    val end = calendar.getTime
    (start, end)
  }

  def getLastMonthDateRange(): (Date, Date) ={
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    calendar.set(Calendar.DATE, 1)
    val start = calendar.getTime
    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    val end = calendar.getTime
    (start, end)
  }


  def fetchPricesFor(range: String) = Action { request =>

    val reqeustedRange:(Date, Date) = range match {
      case "lastweek" => getLastWeekDateRange()
      case "lastmonth" => getLastMonthDateRange()
      case x if x contains "-" =>
        Try {
          val fromDateStr = x.split("-")(0)
          val toDateStr = x.split("-")(1)
          val fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(fromDateStr)
          val toDate = new SimpleDateFormat("dd/MM/yyyy").parse(toDateStr)
          (fromDate, toDate)
        }.getOrElse(getLastWeekDateRange())
      case _ => getLastWeekDateRange()
    }

    Ok(Json.obj("status" -> "OK", "data" -> DataLoader.getPastData(reqeustedRange._2, reqeustedRange._1)))
  }

  def getMovingAvg(from: String, to: String) = Action { request =>
    val prediction = Prediction.getPricePrediction(from)
    Ok(Json.obj("status" -> "OK", "prediction" -> prediction))
    //    Ok(s"Got: $from - $to")
  }

  def forecast = Action {
    Ok("predictions")
  }

  def getPriceMovement = Action{ request =>
    Prediction.train(false)
    Ok(Json.obj("status" -> "OK"))
  }

}

package controllers

import java.util.Date

import models.{DataLoader, MovingAverage, PricePrediction}
import play.api.libs.json._
import play.api.mvc._
import utils.Helpers._

import scala.util.Try
/**
  * Created by Sudheer on 01/10/17.
  */

object Application extends Controller {

  def fetchPricesFor(range: String) = Action { request =>
    val reqeustedRange:Option[(Date, Date)] = range match {
      case "lastweek" => Some(getLastWeekDateRange())
      case "lastmonth" => Some(getLastMonthDateRange())
      case x if x contains "-" =>
        Try {
          val fromDateStr = x.split("-")(0)
          val toDateStr = x.split("-")(1)
          val fromDate = dateFormatter.parse(fromDateStr)
          val toDate = dateFormatter.parse(toDateStr)
          if(fromDate.compareTo(toDate) <= 0) Some((fromDate, toDate))
          else None
        }.getOrElse(None)
      case _ => None
    }
    reqeustedRange match {
      case Some(x) => Ok(Json.obj("status" -> "OK", "data" -> DataLoader.getPastData(x._1, x._2)))
      case _ => BadRequest(Json.obj("status"->"error", "message"-> "Input format should be range=lastweek or range=lastweek or range =dd/MM/yyyy-dd/MM/yyyy(daterange in this format) "))
    }


  }

  def getMovingAvg(from: String, to: String, window:Int) = Action { request =>
    val fromDate = dateFormatter.parse(from)
    val toDate = dateFormatter.parse(to)
    if(fromDate.compareTo(toDate) >= 0 || window < 1){
      BadRequest(Json.obj("status"->"error", "message"-> "Input format should be range=lastweek or range=lastweek or range =dd/MM/yyyy-dd/MM/yyyy(daterange in this format)"))
    }
    else {
      val movingAvg = MovingAverage.getMovingAverageFromRange(fromDate, toDate, window)
      Ok(Json.obj("status" -> "OK", "data" -> movingAvg))
    }
  }

  def forecast = Action {
    val prediction = PricePrediction.getPricePrediction
    Ok(Json.obj("status" -> "OK", "prediction" -> prediction))
  }

  def ping = Action {
    Ok(Json.obj("status" -> "OK"))
  }

}

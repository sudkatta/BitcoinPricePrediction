package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import models.{DataLoader, SparkEngine}
import play.api.libs.json._
import play.api.mvc._

import scala.util.Try
import utils.Helpers._
/**
  * Created by Sudheer on 01/10/17.
  */

object Application extends Controller {

  def fetchPricesFor(range: String) = Action { request =>
    val reqeustedRange:Option[(Date, Date)] = range match {
      case "lastweek" => Some(getLastWeekDateRange)
      case "lastmonth" => Some(getLastMonthDateRange)
      case x if x contains "-" =>
        Try {
          val fromDateStr = x.split("-")(0)
          val toDateStr = x.split("-")(1)
          val fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(fromDateStr)
          val toDate = new SimpleDateFormat("dd/MM/yyyy").parse(toDateStr)
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
    val format = new java.text.SimpleDateFormat("dd/MM/yyyy")
    val fromDate = format.parse(from)
    val toDate = format.parse(to)
    if(fromDate.compareTo(toDate) >= 0 || window < 1){
      BadRequest(Json.obj("status"->"error", "message"-> "Input format should be range=lastweek or range=lastweek or range =dd/MM/yyyy-dd/MM/yyyy(daterange in this format)"))
    }
    else {
      val movingAvg = SparkEngine.getMovingAverageFromRange(fromDate, toDate, window)
      Ok(Json.obj("status" -> "OK", "data" -> movingAvg))
    }
  }

  def forecast = Action {
    val prediction = SparkEngine.getPricePrediction
    Ok(Json.obj("status" -> "OK", "prediction" -> prediction))
  }

  def getPriceMovement = Action{ request =>
    SparkEngine.train
    Ok(Json.obj("status" -> "OK"))
  }

}

package utils

import java.text.SimpleDateFormat
import java.util.Calendar._
import java.util.{Calendar, Date}

import org.specs2.matcher.MatchersImplicits
import org.specs2.mutable.Specification
import utils.Helpers._

/**
  * Created by skatta on 10/3/2017 AD.
  */
class HelpersTest extends Specification with MatchersImplicits{
  sequential

  val calendar:Calendar = Calendar.getInstance()
  val dateFormatter = new SimpleDateFormat("dd/MM/yyyy")


  def beCloseInTimeTo(date1: Date, date2: Date) = {
    date1.getTime/1000 must beBetween(date2.getTime/1000 - 500, date2.getTime/1000 + 500)
  }

  "HelpersTest" should {

    "getLastMonthDateRange" in {
      calendar.set(2017,OCTOBER,15,0,0,0)
      val fromDate = dateFormatter.parse("31/08/2017")
      val toDate = dateFormatter.parse("30/09/2017")
      val result = Helpers.getLastMonthDateRange(calendar = calendar)
      beCloseInTimeTo(result._1, fromDate)
      beCloseInTimeTo(result._2, toDate)
    }

    "round" in {
      val in = 10.90032
      val out = 10.90
      round(in) mustEqual out
    }

    "getLastWeekDateRange" in {
      calendar.set(2017,OCTOBER,15,0,0,0)
      val fromDate = dateFormatter.parse("08/10/2017")
      val toDate = dateFormatter.parse("15/10/2017")
      val result = Helpers.getLastWeekDateRange(calendar = calendar)
      beCloseInTimeTo(result._1, fromDate)
      beCloseInTimeTo(result._2, toDate)
    }

    "dateAddDays" in {
      val dateFormatter = new SimpleDateFormat("dd/MM/yyyy")
      val inDate = dateFormatter.parse("31/08/2017")
      val outDate = dateFormatter.parse("05/09/2017")
      val result = Helpers.dateAddDays(inDate, 5)
      beCloseInTimeTo(result, outDate)
    }

  }
}

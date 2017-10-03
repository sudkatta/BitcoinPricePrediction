package utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

/**
  * Created by skatta on 10/2/2017 AD.
  */
object Helpers {

  val dateFormatter = new SimpleDateFormat("dd/MM/yyyy")

  def getLastWeekDateRange(calendar:Calendar = Calendar.getInstance()): (Date, Date) ={
    val i = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek
    calendar.add(Calendar.DATE, -i - 7)
    val start = calendar.getTime
    calendar.add(Calendar.DATE, 7)
    val end = calendar.getTime
    (start, end)
  }

  def getLastMonthDateRange(calendar:Calendar = Calendar.getInstance()): (Date, Date) ={
    calendar.add(Calendar.MONTH, -1)
    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    val end = calendar.getTime
    calendar.add(Calendar.MONTH, -1)
    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    val start = calendar.getTime
    (start, end)
  }

  def getLastMonthCount(calendar:Calendar = Calendar.getInstance()): Int ={
    calendar.add(Calendar.MONTH, -1)
    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  def round(x:Double):Double= BigDecimal(x).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  def dateAddDays(date: Date, days:Int):java.sql.Date={
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    calendar.add(Calendar.DATE, days)
    calendar.getTime
    new java.sql.Date(calendar.getTimeInMillis)
  }
}

package utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

/**
  * Created by skatta on 10/2/2017 AD.
  */
object Helpers {

  val dateFormatter = new SimpleDateFormat("dd/MM/yyyy")

  def getLastWeekDateRange: (Date, Date) ={
    val calendar = Calendar.getInstance()
    val i = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek
    calendar.add(Calendar.DATE, -i - 7)
    val start = calendar.getTime
    calendar.add(Calendar.DATE, 7)
    val end = calendar.getTime
    (start, end)
  }

  def getLastMonthDateRange: (Date, Date) ={
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    val end = calendar.getTime
    calendar.add(Calendar.MONTH, -1)
    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    val start = calendar.getTime

    (start, end)
  }

  def getLastMonthCount: Int ={
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  def round(x:Double):Double= BigDecimal(x).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  def dateSubtractDays(date: Date, days:Int):Date={
    val dateAux = Calendar.getInstance()
    dateAux.setTime(date)
    dateAux.add(Calendar.DATE, -days)
    dateAux.getTime
    new java.sql.Date(dateAux.getTimeInMillis)
  }

  def dateAddDays(date: Date, days:Int):java.sql.Date={
    val dateAux = Calendar.getInstance()
    dateAux.setTime(date)
    dateAux.add(Calendar.DATE, days)
    dateAux.getTime
    new java.sql.Date(dateAux.getTimeInMillis)
  }
}

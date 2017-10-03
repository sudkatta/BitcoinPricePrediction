package models

import java.text.SimpleDateFormat

import org.specs2.mutable.Specification
import views.html.helper.input

/**
  * Created by skatta on 10/3/2017 AD.
  */
class MovingAverageTest extends Specification {

  val dateFormatter = new SimpleDateFormat("dd/MM/yyyy")

  val date1 = dateFormatter.parse("10/05/2017")
  val date2 = dateFormatter.parse("11/05/2017")
  val date3 = dateFormatter.parse("12/05/2017")
  val date4 = dateFormatter.parse("13/05/2017")
  val date5 = dateFormatter.parse("14/05/2017")
  val date6 = dateFormatter.parse("15/05/2017")

  val inputPricePoints:List[PricePoint] = List(PricePoint(1, date1),PricePoint(5, date2),PricePoint(3, date3),PricePoint(1, date4),PricePoint(10, date5),
    PricePoint(-1, date6), PricePoint(10, date1))


    "MovingAverageTest" should {

    "getMovingAverageFromList" in {
      MovingAverage.getMovingAverageFromList(inputPricePoints, 1).sortBy(x=> x.date) mustEqual inputPricePoints.sortBy(x=> x.date)

      val outputPricePoints:List[PricePoint] = List(PricePoint(4.75, date3), PricePoint(2.5, date4), PricePoint(4.75, date5), PricePoint(3.25, date6))
      MovingAverage.getMovingAverageFromList(inputPricePoints, 4).sortBy(x=> x.date) mustEqual outputPricePoints.sortBy(x=> x.date)

    }

  }
}

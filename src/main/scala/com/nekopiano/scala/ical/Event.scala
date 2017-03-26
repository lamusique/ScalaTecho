package com.nekopiano.scala.ical

import com.github.nscala_time.time.Imports._
import net.fortuna.ical4j.model.Component
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{DateTime, LocalDate, Period, PeriodType}

/**
  * Created on 09/Jan/2016.
  */
case class Event(val component:Component, val eventType:EventType) {}

class WorkingTimeEvent(val client:String, component:Component, eventType:EventType, val date:LocalDate, val start:DateTime, val end:DateTime, val description:String) extends Event(component, eventType) {

  private val DATE_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd")
  private val TIME_FORMAT = DateTimeFormat.forPattern("HH:mm")

  private val PERIOD_FORMATTER = {
    val builder = new PeriodFormatterBuilder()
    builder.minimumPrintedDigits(2)
    builder.printZeroAlways()
    builder.appendHours()
    builder.appendLiteral(":")
    builder.appendMinutes()
    builder.toFormatter
  }

  def startTime() = start.toString(TIME_FORMAT)

  def endPeriod() = {
    //val period = new Period(start, end, PeriodType.dayTime())
    val period = this.period()
    val hours = period.getHours + period.getMinutes.toDouble / 60
    val startPeriod = PERIOD_FORMATTER.parsePeriod(startTime())
    val endPeriod = startPeriod.plus(period).normalizedStandard(PeriodType.time())
    PERIOD_FORMATTER.print(endPeriod)
  }


  def breakTime() = {
    // Find a lunchtime
    val today = start.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
    val lunchtime = new Interval(today.withHourOfDay(12), today.withHourOfDay(13))

    val interval = new Interval(start, end)
    val breakTime = interval.overlap(lunchtime) match {
      case null => 0.0
      case _ => 1.0
    }
    breakTime
  }

  def hours() = {
    val period = this.period()
    val hours = period.getHours + period.getMinutes.toDouble / 60
    hours
  }

  def workingHours() = {
    hours() - breakTime()
  }

  private def period() = new Period(start, end, PeriodType.dayTime())


  def value() = (start.toString(DATE_FORMAT), startTime, endPeriod(), breakTime, workingHours(), client, description)


}

object EventType {
  // singletons
  case object EVENT extends EventType(0)
  case object BILLABLE extends EventType(1)
  case object WORK extends EventType(2)
  case object NOTES extends EventType(3)
  case object MEETING extends EventType(4)
  case object MISC extends EventType(5)
}
sealed abstract class EventType(val code:Int) {
  val name = toString
}

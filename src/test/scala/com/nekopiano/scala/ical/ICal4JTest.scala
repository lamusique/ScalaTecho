package com.nekopiano.scala.ical

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.{Property, Component}

import com.github.nscala_time.time.Imports._
import org.joda.time.PeriodType
import org.joda.time.format.PeriodFormatterBuilder

/**
 * Created by Neko Piano at 7:21 PM 7/31/15.
 */
class ICal4JTest extends org.specs2.mutable.Specification {

  private val UTC_FORMAT = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ")
  private val TIME_FORMAT = DateTimeFormat.forPattern("HH:mm")
  private val DATE_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd")

  private val PERIOD_FORMATTER = {
    val builder = new PeriodFormatterBuilder()
    builder.minimumPrintedDigits(2)
    builder.printZeroAlways()
    builder.appendHours()
    builder.appendLiteral(":")
    builder.appendMinutes()
    builder.toFormatter
  }

  "this is my specification" >> {
    "where example 1 must be true" >> {

      val source = scala.io.Source.fromURL(getClass().getResource("/billable.ics"), "UTF-8")
      val builder = new CalendarBuilder
      val calendar = builder.build(source.bufferedReader)

      import scala.collection.JavaConverters._

      val components = calendar.getComponents.iterator.asInstanceOf[java.util.Iterator[Component]].asScala.toIndexedSeq
      val lines = components map (component => {
        val summary = component.getProperty("SUMMARY").getValue
        val splitSummary = summary.split(": ")
        val start = DateTime.parse(component.getProperty("DTSTART").getValue, UTC_FORMAT)
        val startTime = start.toString(TIME_FORMAT)
        val end = DateTime.parse(component.getProperty("DTEND").getValue, UTC_FORMAT)
        val period = new Period(start, end, PeriodType.dayTime())
        val hours = period.getHours + period.getMinutes.toDouble / 60
        val startPeriod = PERIOD_FORMATTER.parsePeriod(startTime)
        val endPeriod = startPeriod.plus(period).normalizedStandard(PeriodType.time())
        (start.toString(DATE_FORMAT), startTime, PERIOD_FORMATTER.print(endPeriod), hours, splitSummary(0), splitSummary(1))
      })

      val treatedLines = lines.reverse.map(line =>{
        line.productIterator.toList.mkString("\t")
      })

      treatedLines foreach println

      1 must_== 1
    }
    "where example 2 must be true" >> {

      val date = DateTime.parse("20100603T120000Z", UTC_FORMAT)
      println(date)
      println(date.toDate)

      println(TIME_FORMAT.print(date))

      2 must_== 2
    }
  }
}

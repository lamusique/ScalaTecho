package com.nekopiano.scala.ical

import com.github.nscala_time.time.Imports._
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.util.CompatibilityHints
import org.joda.time.PeriodType
import org.joda.time.format.PeriodFormatterBuilder

/**
 * Created at 1/Sep/16.
 */
class ICal4JTimesheetTest extends org.specs2.mutable.Specification {

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

      val source = scala.io.Source.fromURL(getClass().getResource("/timesheet.ics"), "UTF-8")

      CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true)
      val builder = new CalendarBuilder
      val calendar = builder.build(source.bufferedReader)

      import scala.collection.JavaConverters._

      val components = calendar.getComponents.iterator.asInstanceOf[java.util.Iterator[Component]].asScala.toIndexedSeq
      val events = components.map(component => {
        val summary = component.getProperty("SUMMARY").getValue
        val eventType = summary match {
          case summary:String if {
            val splitSummary = summary.split(": ")
            splitSummary.size > 1} => EventType.BILLABLE
          case _ => EventType.MISC
        }
        new Event(component, eventType)
      })

      val billableEvents = events.filter(_.eventType == EventType.BILLABLE)

      val lines = billableEvents map (event => {
        val component = event.component
        val summary = component.getProperty("SUMMARY").getValue
        val splitSummary = summary.split(": ")
        val start = DateTime.parse(component.getProperty("DTSTART").getValue, UTC_FORMAT)
        val startTime = start.toString(TIME_FORMAT)
        val end = DateTime.parse(component.getProperty("DTEND").getValue, UTC_FORMAT)

        // Find a lunchtime
        val today = start.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        val lunchtime = new Interval(today.withHourOfDay(12), today.withHourOfDay(13))

        val interval = new Interval(start, end)
        val breakTime = interval.overlap(lunchtime) match {
          case null => 0.0
          case _ => 1.0
        }

        // endTime could be over 24:00, e.g. 32:00
        val period = new Period(start, end, PeriodType.dayTime())
        val hours = period.getHours + period.getMinutes.toDouble / 60
        val startPeriod = PERIOD_FORMATTER.parsePeriod(startTime)
        val endPeriod = startPeriod.plus(period).normalizedStandard(PeriodType.time())

        (start.toString(DATE_FORMAT), startTime, PERIOD_FORMATTER.print(endPeriod), breakTime, hours, splitSummary(0), splitSummary(1))
      })

      val treatedLines = lines.reverse.map(line =>{
        line.productIterator.toList.mkString("\t")
      })

      treatedLines foreach println


      //lines.groupBy()




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

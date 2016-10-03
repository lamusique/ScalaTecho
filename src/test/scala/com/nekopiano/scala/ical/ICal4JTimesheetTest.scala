package com.nekopiano.scala.ical

import com.github.nscala_time.time.Imports._
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.util.CompatibilityHints
import org.joda.time.{DateTime, DurationFieldType, LocalDate, PeriodType}
import org.joda.time.format.PeriodFormatterBuilder

import scala.collection.immutable.IndexedSeq

/**
 * Created at 1/Sep/16.
 */
class ICal4JTimesheetTest extends org.specs2.mutable.Specification {

  private val UTC_FORMAT = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ")
  private val TIME_FORMAT = DateTimeFormat.forPattern("HH:mm")
  private val DATE_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd")
  private val DAY_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd E")


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

      val billableEvents = events
        .filter(_.eventType == EventType.BILLABLE)
        // YYYYMMDD is for an all day event.
        .filter(_.component.getProperty("DTSTART").getValue.size > 8)

      println("=" * 64)
      println("All work")
      println("=" * 64)

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

        new WorkingTimeEvent(splitSummary(0), component, EventType.BILLABLE, start.toLocalDate, start, end, splitSummary(1))
        //(start.toString(DATE_FORMAT), startTime, PERIOD_FORMATTER.print(endPeriod), breakTime, hours, splitSummary(0), splitSummary(1), component)
      })

      val linesSortedByDateTime = lines.sortBy(_.start)
      val treatedLines = linesSortedByDateTime.map(line =>{
        line.value.productIterator.toList.mkString("\t")
      })

      treatedLines foreach println


      println("=" * 64)
      println("all dates aligned")
      println("=" * 64)

      val startDate = linesSortedByDateTime.head.start
      val endDate = linesSortedByDateTime.last.start
      val duration = new Duration(startDate, endDate).getStandardDays.toInt
      val days = (0 to duration).map(day => startDate.plusDays(day.toInt).toLocalDate)

      val dateMapping = linesSortedByDateTime.map(line => line.date -> line) toMap

      val allDays = days.map(day => {
        day -> dateMapping.get(day)
      })
      allDays.foreach(day => {
        val line = day._2 match {
          case Some(line) => line.productIterator.toList.mkString("\t")
          case None => ""
        }
        val dayString = DAY_FORMAT.print(day._1)

        //println()
      })


      println("=" * 64)
      println("Group by Client")
      println("=" * 64)

      val linesGroupedByClient = linesSortedByDateTime.groupBy(_.client).map(group => {
        val lines = group._2.map(_.value.productIterator.toList.mkString("\t"))
        (group._1, lines)
      })
      linesGroupedByClient.foreach(group=>{
        println("=" * 32)
        println(group._1)
        println("=" * 32)
        println(group._2.mkString("\n"))
      })

      val linesGroupedByDate = linesSortedByDateTime.groupBy(_.date).toSeq.sortBy(_._1)

      val stringLinesGroupedByDate = linesGroupedByDate.map(group => {
        val lines = group._2.map(_.value.productIterator.toList.mkString("\t"))
        (group._1, lines)
      }).toSeq.sortBy(_._1)
      stringLinesGroupedByDate.foreach(group=>{
        println("=" * 32)
        println(group._1)
        println("=" * 32)
        println(group._2.mkString("\n"))
      })

      println("=" * 64)
      println("Merged Dates")
      println("=" * 64)

      val mergedByDate = linesGroupedByDate.map(group => {
        val events: IndexedSeq[WorkingTimeEvent] = group._2
        val workingHours = events.map(_.workingHours()).sum
        val breakTime = events.map(_.breakTime()).sum
        val concatenatedDescription = events.map(line => {
          line.client + ": " + line.description
        }).mkString("; ")

        val workingHoursByClient = events.groupBy(_.client).map(group=>{
          (group._1, group._2.map(_.workingHours()).sum)
        })

       (events.head.date, events.head.startTime(), events.last.endPeriod(), breakTime, workingHours, concatenatedDescription, workingHoursByClient)
      })


      val clients = mergedByDate.flatMap(oneDay => oneDay._7.keySet).distinct.toSeq.sorted

      val hoursByClient = mergedByDate.map(oneDay => {
        val client2hours = oneDay._7
        val hoursByClient = clients.map(client => {
          client2hours.getOrElse(client, 0.0)
        })
        (oneDay, hoursByClient)
      })

      val fixedHeader = Seq("date", "start", "end", "break time", "working hours", "description")
      val header = fixedHeader ++ clients
      println(header.mkString("\t"))

      val stringLines = hoursByClient.map(oneDay => {
        val normalColumns = oneDay._1.productIterator.toSeq.dropRight(1)
        //val normalColumns = oneDay._1.productIterator.toSeq.dropRight(1).asInstanceOf[(LocalDate, String, String, Double, Double, String)]
        val hoursByClients = oneDay._2
        (normalColumns ++ hoursByClients) mkString("\t")
      })
      stringLines foreach println


      println("=" * 64)
      println("taget month time sheet")
      println("=" * 64)

      val hoursByClientInTargetMonth = hoursByClient.filter(_._1._1.isAfter(LocalDate.parse("2016-09-01").minusDays(1)))

      val targetMonthLines = hoursByClientInTargetMonth.map(oneDay => {
        val normalColumns = oneDay._1.productIterator.toSeq.dropRight(1)
        //val normalColumns = oneDay._1.productIterator.toSeq.dropRight(1).asInstanceOf[(LocalDate, String, String, Double, Double, String)]
        val hoursByClients = oneDay._2
        (normalColumns ++ hoursByClients) mkString("\t")
      })
      targetMonthLines foreach println


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

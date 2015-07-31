package com.nekopiano.scala.ical

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.{Property, Component}

import com.github.nscala_time.time.Imports._
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatter}

/**
 * Created by Neko Piano at 7:21 PM 7/31/15.
 */
class ICal4JTest extends org.specs2.mutable.Specification {

  //private val UTC_PATTERN: String = "yyyyMMdd'T'HHmmss'Z'"
  private val UTC_PATTERN: String = "yyyyMMdd'T'HHmmssZ"

  "this is my specification" >> {
    "where example 1 must be true" >> {

      val source = scala.io.Source.fromURL(getClass().getResource("/test.ics"), "UTF-8")
      val builder = new CalendarBuilder
      val calendar = builder.build(source.bufferedReader)

      import scala.collection.JavaConverters._

      val components = calendar.getComponents.iterator.asInstanceOf[java.util.Iterator[Component]].asScala.toIndexedSeq
      components map (component => {
        println("component.getName=" + component.getName)
        component.getProperty("DTSTART").getValue
        component.getProperty("DTEND").getValue

//        val properties = component.getProperties.iterator.asInstanceOf[java.util.Iterator[Property]].asScala.toIndexedSeq
//        properties map (property => {
//          println("property=" + property.getName + property.getValue)
//        })


      })

      val date = DateTime.parse("20100603T120000Z", DateTimeFormat.forPattern(UTC_PATTERN))
      println(date)
      println(date.toDate)
      val dateManually = DateTime.parse("20100603T120000Z", DateTimeFormat.forPattern(UTC_PATTERN)).withZone(DateTimeZone.UTC)
      println(dateManually)
      println(dateManually)

//      val dt = ISODateTimeFormat.dateTime().parseDateTime("20100603T120000Z")
//      println(dt)
//      // startDate = "2013-07-12T18:31:01.000Z";
//      val dtManually = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime("20100603T120000Z");
//      println(dtManually)

      val df = DateTimeFormat.forPattern(UTC_PATTERN)
      val offsetDt = df.withOffsetParsed().parseDateTime("20100603T120000Z");
      println(offsetDt)
      println(offsetDt.toDate)

      1 must_== 1
    }
    "where example 2 must be true" >> {
      2 must_== 2
    }
  }
}

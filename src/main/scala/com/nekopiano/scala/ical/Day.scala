package com.nekopiano.scala.ical

import org.joda.time.LocalDate

/**
  * Created on 26/03/2017.
  */
case class Day (val date:LocalDate, val events:List[Event]) {}

//Error:(8, 12) case class WorkingDay has case ancestor com.nekopiano.scala.ical.Day, but case-to-case inheritance is prohibited. To overcome this limitation, use extractors to pattern match on non-leaf nodes.
//case class WorkingDay (override val events:List[WorkingTimeEvent]) extends Day(events) {}

class WorkingDay (override val date:LocalDate, override val events:List[WorkingTimeEvent]) extends Day(date, events) {}

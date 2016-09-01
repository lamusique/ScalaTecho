package com.nekopiano.scala.ical

import net.fortuna.ical4j.model.Component

/**
  * Created on 01/09/2016.
  */
class Event(val component:Component, val eventType:EventType) {}
class WorkingTimeEvent(val client:String, component:Component, eventType:EventType) extends Event(component, eventType) {}

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
  // A, C, G, T, Nをcase objectとすると、クラス名を表示するtoStringが実装される
  val name = toString
}

name := "iCalScala"

version := "1.0"

scalaVersion := "2.11.7"


// Read here for optional jars and dependencies
libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "3.6.4" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

// For Java
// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
  "org.mnode.ical4j" % "ical4j" % "1.0.6"
)

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.0.0"


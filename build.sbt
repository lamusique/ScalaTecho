name := "scala-techo"
// Previously iCalScala

version := "1.0"

scalaVersion := "2.11.8"


// Read here for optional jars and dependencies
libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "3.8.4" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")

// For Java
// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
  "org.mnode.ical4j" % "ical4j" % "1.0.7",
    // for non-standard properties, e.g. ACKNOWLEDGED
  "org.mnode.ical4j" % "ical4j-extensions" % "0.9.2"
)

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.12.0"


name := "scala-techo"
// Previously iCalScala

version := "1.0"

scalaVersion := "2.12.1"



// Read here for optional jars and dependencies
libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "3.8.7" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")



libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.16.0"

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"



// For Java
// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
  "org.mnode.ical4j" % "ical4j" % "1.0.7",
    // for non-standard properties, e.g. ACKNOWLEDGED
  "org.mnode.ical4j" % "ical4j-extensions" % "0.9.2"
)

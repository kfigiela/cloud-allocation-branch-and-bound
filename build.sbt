
scalaVersion := "2.10.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "Typesafe Repository R" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-zeromq" % "2.2-SNAPSHOT"

resolvers += "Sonatype (releases)" at "https://oss.sonatype.org/content/repositories/releases/"

libraryDependencies += "org.zeromq" %% "zeromq-scala-binding" % "0.0.6"
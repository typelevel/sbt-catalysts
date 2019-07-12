addSbtPlugin("org.portable-scala"  %  "sbt-scalajs-crossproject" % "0.6.1")

unmanagedSourceDirectories in Compile += file("src/main/scala/org/typelevel").getAbsoluteFile
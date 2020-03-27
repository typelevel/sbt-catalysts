addSbtPlugin("org.portable-scala"  %  "sbt-scalajs-crossproject" % "1.0.0")

unmanagedSourceDirectories in Compile += file("src/main/scala/org/typelevel").getAbsoluteFile
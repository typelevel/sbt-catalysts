addSbtPlugin("org.portable-scala"  %  "sbt-scalajs-crossproject" % "1.0.0")

Compile/ unmanagedSourceDirectories  += file("src/main/scala/org/typelevel").getAbsoluteFile
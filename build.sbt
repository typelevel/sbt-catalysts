
sbtPlugin := true
organization := "org.typelevel"
name := "sbt-catalysts"
description := "SBT plugin for Catalysts"

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.10"

addSbtPlugin("com.eed3si9n"        %  "sbt-unidoc"             % "0.4.1")
addSbtPlugin("com.github.gseitz"   %  "sbt-release"            % "1.0.6")
addSbtPlugin("com.github.tkawachi" %  "sbt-doctest"            % "0.7.1")
addSbtPlugin("org.xerial.sbt"      %  "sbt-sonatype"           % "2.0")
addSbtPlugin("com.jsuereth"        %  "sbt-pgp"                % "1.1.0")
addSbtPlugin("pl.project13.scala"  %  "sbt-jmh"                % "0.2.27")
addSbtPlugin("org.scalastyle"      %% "scalastyle-sbt-plugin"  % "1.0.0")
addSbtPlugin("org.scoverage"       %  "sbt-scoverage"          % "1.5.1")
addSbtPlugin("com.typesafe.sbt"    %  "sbt-git"                % "0.9.3")
addSbtPlugin("org.scala-js"        %  "sbt-scalajs"            % "0.6.21")
addSbtPlugin("com.47deg"           %  "sbt-microsites"         % "0.7.6")
addSbtPlugin("io.get-coursier"     %  "sbt-coursier"            % "1.0.0")

scalacOptions ++= Seq(Opts.compile.deprecation, "-feature")

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

scmInfo := Some(ScmInfo(url("https://github.com/inthenow/sbt-catalysts"), "git@github.com:inthenow/sbt-catalysts.git"))

publishMavenStyle := false
bintrayRepository := "sbt-plugins"
bintrayOrganization := Some("typelevel")

scriptedLaunchOpts ++= List("-Xms1024m", "-Xmx1024m", "-XX:ReservedCodeCacheSize=128m", "-XX:MaxPermSize=256m", "-Xss2m", "-Dfile.encoding=UTF-8")

resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)

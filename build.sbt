
organization := "org.typelevel"
name := "sbt-catalysts"
description := "SBT plugin for Catalysts"

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.30"
enablePlugins(SbtPlugin)
addSbtPlugin("com.eed3si9n"        %  "sbt-unidoc"               % "0.4.3")
addSbtPlugin("com.github.gseitz"   %  "sbt-release"              % "1.0.13")
addSbtPlugin("com.github.tkawachi" %  "sbt-doctest"              % "0.9.9")
addSbtPlugin("org.xerial.sbt"      %  "sbt-sonatype"             % "3.9.5")
addSbtPlugin("com.jsuereth"        %  "sbt-pgp"                  % "2.1.1")
addSbtPlugin("pl.project13.scala"  %  "sbt-jmh"                  % "0.4.0")
addSbtPlugin("org.scalastyle"      %% "scalastyle-sbt-plugin"    % "1.0.0")
addSbtPlugin("org.scoverage"       %  "sbt-scoverage"            % "1.6.1")
addSbtPlugin("com.typesafe.sbt"    %  "sbt-git"                  % "1.0.0")
addSbtPlugin("org.portable-scala"  %  "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"        %  "sbt-scalajs"              % "1.4.0")
addSbtPlugin("com.47deg"           %  "sbt-microsites"           % "1.3.2")

scalacOptions ++= Seq(Opts.compile.deprecation, "-feature")

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

scalaVersion := "2.12.12"

scmInfo := Some(ScmInfo(url("https://github.com/inthenow/sbt-catalysts"), "git@github.com:inthenow/sbt-catalysts.git"))

publishMavenStyle := false
bintrayRepository := "sbt-plugins"


bintrayOrganization := Some("typelevel")

scriptedBufferLog := false

scriptedLaunchOpts :=  Seq(
  "-Xmx1024M",
  "-XX:MaxPermSize=256M",
  "-Dplugin.version=" + version.value
)

//self referencing so that dependencies can be monitored by Scala Steward
org.typelevel.libraries.testDependencies(org.typelevel.libraries.libs.keys.toSeq:_*)

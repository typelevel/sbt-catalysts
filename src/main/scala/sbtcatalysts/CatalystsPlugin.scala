package sbtcatalysts

import sbt._
import Keys._

import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.sbt.pgp.PgpKeys.publishSigned
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbt.SbtGit._

import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtGhPages.ghpages

import tut.Plugin._
import pl.project13.scala.sbt.SbtJmh._
import sbtunidoc.Plugin.UnidocKeys._
import sbtunidoc.Plugin._
import sbtrelease.ReleasePlugin.autoImport._
import ReleaseTransformations._
import org.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin.autoImport._
import scoverage.ScoverageSbtPlugin._
import org.scalajs.sbtplugin.cross.{CrossProject, CrossType}

object CatalystsPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

   object autoImport extends CatalystsBase {
  }
}

trait CatalystsBase {
  type VersionsType = Map[String, String]
  type LibrariesType = Map[String, (String, String, String)]

  // Licences
  val apache = ("Apache License", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
  val mit = ("MIT", url("http://opensource.org/licenses/MIT"))

  // Github settings and related settings usually found in a Github README
  case class GitHubSettings(org: String, proj: String, publishOrg: String, license: (String, URL)) {

    def home = s"https://github.com/$org/$proj"
    def repo = s"git@github.com:$org/$proj.git"
    def api  = s"https://$org.github.io/$proj/api/"
    def organisation  = s"com.github.$org"
    override def toString = s"GitHubSettings:home = $home\nGitHubSettings:repo = $repo\nGitHubSettings:api = $api\nGitHubSettings:organisation = $organisation"
  }

  // From https://github.com/typelevel/sbt-typelevel/blob/master/src/main/scala/Developer.scala
  case class Dev(name: String, id: String) {
    def pomExtra: xml.NodeSeq = (
      <developer>
        <id>{ id }</id>
        <name>{ name }</name>
        <url>http://github.com/{ id }</url>
          </developer>
    )
  }

  // These three addLib methods need DRYing
  def addLibs(vl: (VersionsType, LibrariesType), s: String*) = {
    s.map(s => Seq(libraryDependencies += vl._2(s)._2 %%% vl._2(s)._3  % vl._1(vl._2(s)._1) )).flatten
  }

  def addCompileLibs(vl: (VersionsType, LibrariesType), s: String*) = {
    s.map(s => Seq(libraryDependencies += vl._2(s)._2 %%% vl._2(s)._3  % vl._1(vl._2(s)._1)  % "compile")).flatten
  }

  def addTestLibs(vl: (VersionsType, LibrariesType), s: String*) = {
    s.map(s => Seq(libraryDependencies += vl._2(s)._2 %%% vl._2(s)._3  % vl._1(vl._2(s)._1)  % "test")).flatten
  }

  // Common and shared setting 
  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )

  lazy val rootSettings = noPublishSettings ++ Seq(
    moduleName := "root"
  )

  lazy val crossVersionSharedSources: Seq[Setting[_]] =
    Seq(Compile, Test).map { sc =>
      (unmanagedSourceDirectories in sc) ++= {
        (unmanagedSourceDirectories in sc ).value.map {
          dir:File => new File(dir.getPath + "_" + scalaBinaryVersion.value)
        }
      }
    }

  def scalaMacrosParadise(version: String): Seq[Setting[_]] = Seq(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
    libraryDependencies += compilerPlugin("org.scalamacros" %% "paradise" % version cross CrossVersion.full)
  )

  def scalaMacroDependencies(version: String): Seq[Setting[_]] = Seq(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
        case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
        // in Scala 2.10, quasiquotes are provided by macro paradise
        case Some((2, 10)) =>
          Seq(
            compilerPlugin("org.scalamacros" %% "paradise" % version cross CrossVersion.full),
                "org.scalamacros" %% "quasiquotes" % version cross CrossVersion.binary
          )
      }
    }
  )

  lazy val scalacCommonOptions = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked"
  )

  lazy val scalacLanguageOptions = Seq(
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros"
  )

  lazy val scalacStrictOptions = Seq(
    "-Xfatal-warnings",
    "-Xlint",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  )

  lazy val scalacAllOptions = scalacCommonOptions ++ scalacLanguageOptions ++ scalacStrictOptions


  lazy val sharedCommonSettings = Seq(
    updateOptions := updateOptions.value.withCachedResolution(true)
  )

  def sharedBuildSettings(gh: GitHubSettings, vers: VersionsType) = Seq(
    organization := gh.publishOrg,
    scalaVersion := vers("scalac"),
    crossScalaVersions := Seq(vers("scalac_2.10"), scalaVersion.value)
  )

  def sharedPublishSettings(gh: GitHubSettings, devs: Seq[Dev]): Seq[Setting[_]] = Seq(
    homepage := Some(url(gh.home)),
    licenses += gh.license,
    scmInfo :=  Some(ScmInfo(url(gh.home), "scm:git:" + gh.repo)),
    apiURL := Some(url(gh.api)),
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("Snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("Releases" at nexus + "service/local/staging/deploy/maven2")
    },
    autoAPIMappings := true,
    pomExtra := ( <developers> { devs.map(_.pomExtra) } </developers> )
  )

  lazy val sharedReleaseProcess = Seq(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
      pushChanges)
  )

  def sharedScoverageSettings(min: Int = 80) = Seq(
    ScoverageKeys.coverageMinimum := min,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := scalaBinaryVersion.value != "2.10"
    // ScoverageKeys.coverageExcludedPackages := "catalysts\\.bench\\..*"
  )

  lazy val unidocCommonSettings = Seq(
    scalacOptions in (ScalaUnidoc, unidoc) += "-Ymacro-no-expand"
  )

  lazy val warnUnusedImport = Seq(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          Seq()
        case Some((2, n)) if n >= 11 =>
          Seq("-Ywarn-unused-import")
      }
    },
    scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
    scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console))
  )

  lazy val credentialSettings = Seq(
    // For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )

  // Builder methods

  /**
   * Creates a module definition based on a Scala.js CrossProject
   * 
   * The standard Scala.js CrossProject contains the real JVM and JS project
   * and can be used as a dependcy of another CrossProject. But it not an 
   * sbt Project, and cannot be used as one.
   * 
   * So we introduce the concept of a Module, that is essentialy a CrossProject
   * with the difference that the variable name is the directory name with the 
   * suffix "M". This allows us to create a real project based on the dirctory name
   * that is an aggregate project for the underlying JS and JVM projects.
   * 
   * Usage;
   * 
   * In build.sbt, create two helper methods:
   * 
   *   lazy val module = mkModuleFactory(gh.proj, mkConfig(rootSettings, commonJvmSettings, commonJsSettings))
   *   lazy val prj = mkPrjFactory(rootSettings)
   * 
   * For each sub-project (here core is used as an example)
   * 
   *   lazy val core    = prj(coreM)
   *   lazy val coreJVM = coreM.jvm
   *   lazy val coreJS  = coreM.js
   *   lazy val coreM   = module("core",CrossType.Pure)
   *     .dependsOn(testkitM)
   *     .settings(addTestLibs(vlibs, "scalatest"):_*)
   */
  def mkModule(proj: String, projConfig: CrossProject ⇒ CrossProject, id: String,
      crossType: CrossType = CrossType.Pure ): CrossProject = {
    
    val cpId = id.stripSuffix("M")

    CrossProject(cpId, new File(cpId), crossType)
     .settings(moduleName := s"$proj-$id")
    .configure(projConfig)
  }

  /**
   * Helper method for mkModule so that the main project name anf configuration need not ne repeated for each module
   */
  def mkModuleFactory(proj: String, projConfig: CrossProject ⇒ CrossProject) =
    (id: String, crossType: CrossType) => mkModule(proj, projConfig, id, crossType)

  /**
   * Makes a modules aggregate project
   */
  def mkPrj(projSettings: Seq[sbt.Setting[_]], c: CrossProject): Project = {
      val name = c.jvm.id.stripSuffix("JVM")
      val pr: Seq[ProjectReference] = Seq(c.jvm.project,  c.js.project)

      Project(id = name, base = new File(s"${name}/.prj"))
        .settings(noPublishSettings:_*)
        .settings(projSettings)
        .dependsOn(c.jvm, c.js)
        .aggregate(c.jvm, c.js)
   }

  /**
   * Helper method for mkPrj so that the main project settings need not ne repeated for each module
   */
  def mkPrjFactory(projSettings: Seq[sbt.Setting[_]]) = (c: CrossProject) => mkPrj(projSettings, c)

  // Config builders
  def mkConfig(projSettings: Seq[sbt.Setting[_]], jvmSettings: Seq[sbt.Setting[_]],
      jsSettings: Seq[sbt.Setting[_]] ): CrossProject ⇒ CrossProject =
    _.settings(projSettings:_*)
    .jsSettings(jsSettings:_*)
    .jvmSettings(jvmSettings:_*)

  def mkRootConfig(projSettings: Seq[sbt.Setting[_]] , projJVM:Project): Project ⇒ Project =
    _.in(file("."))
    .settings(rootSettings)
    .settings(projSettings)
    .settings(console <<= console in (projJVM, Compile))

  def mkRootJvmConfig(s: String, projSettings: Seq[sbt.Setting[_]],
      jvmSettings: Seq[sbt.Setting[_]]): Project ⇒ Project =
    _.settings(moduleName := s)
    .settings(projSettings)
    .settings(jvmSettings)
    .in(file("." + s + "JVM"))

  def mkRootJsConfig(s: String, projSettings: Seq[sbt.Setting[_]],
      jsSettings: Seq[sbt.Setting[_]]): Project ⇒ Project =
    _.settings(moduleName := s)
    .settings(projSettings)
    .settings(jsSettings)
    .in(file("." + s + "JS"))
    .enablePlugins(ScalaJSPlugin)

  def mkDocConfig(gh: GitHubSettings, projSettings: Seq[sbt.Setting[_]], jvmSettings: Seq[sbt.Setting[_]],
      deps: Project*): Project ⇒ Project =
    _.settings(projSettings)
    .settings(moduleName := gh.proj + "-docs")
    .settings(noPublishSettings)
    .settings(unidocSettings)
    .settings(site.settings)
    .settings(tutSettings)
    .settings(ghpages.settings)
    .settings(jvmSettings)
    .dependsOn(deps.map( ClasspathDependency(_, Some("compile;test->test")))  :_*) //platformJVM, macrosJVM, scalatestJVM, specs2, testkitJVM)
    .settings(
       organization  := gh.organisation,
       autoAPIMappings := true,
       unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(deps.map(_.project)  :_*),
       site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "api"),
       ghpagesNoJekyll := false,
       site.addMappingsToSiteDir(tut, "_tut"),
       tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),

       scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
         "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
         "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
         "-diagrams"
       ),
       git.remoteRepo := gh.repo,
       includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md")
}


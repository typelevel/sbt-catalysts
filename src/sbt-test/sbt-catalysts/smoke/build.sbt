import sbtcrossproject.CrossPlugin.autoImport.CrossType

val apache2 = "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")
val gh = GitHubSettings(org = "kailuowang", proj = "Sheep", publishOrg = "com.kailuowang", license = apache2)


val libs = org.typelevel.libraries




lazy val rootSettings = buildSettings ++ commonSettings ++ publishSettings ++ scoverageSettings
lazy val module = mkModuleFactory(gh.proj, mkConfig(rootSettings, commonJvmSettings, commonJsSettings))
lazy val prj = mkPrjFactory(rootSettings)

lazy val rootJVM = project
  .configure(mkRootJvmConfig(gh.proj, rootSettings, commonJvmSettings))
  .aggregate(coreJVM)
  .settings(noPublishSettings)


lazy val rootJS = project
  .configure(mkRootJsConfig(gh.proj, rootSettings, commonJsSettings))
  .aggregate(coreJS)
  .settings(noPublishSettings)


lazy val core    = prj(coreM)
lazy val coreJVM = coreM.jvm
lazy val coreJS  = coreM.js
lazy val coreM   = module("core", CrossType.Pure)
  .settings(
    libs.dependencies(org.typelevel.libraries.libs.keys.toSeq.filterNot(_ == "scalatest"):_*),  //testing all dependency
    libs.testDependencies("scalatest"),
    simulacrumSettings(libs)
  )

lazy val buildSettings = sharedBuildSettings(gh, libs)

lazy val commonSettings = sharedCommonSettings ++ Seq(
  developers := List(Developer("Kailuo Wang", "@kailuowang", "kailuo.wang@gmail.com", new java.net.URL("http://kailuowang.com"))),
  Test / parallelExecution := false,
  crossScalaVersions := Seq(libs.vers("scalac_2.11"), scalaVersion.value),
  Test / scalacOptions  ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code")))

)  ++ unidocCommonSettings ++
  addCompilerPlugins(libs, "kind-projector")

lazy val commonJsSettings = Seq(Global / scalaJSStage  := FastOptStage)

lazy val commonJvmSettings = Seq()

lazy val publishSettings = sharedPublishSettings(gh) ++ credentialSettings ++ sharedReleaseProcess

lazy val scoverageSettings = sharedScoverageSettings(60)

lazy val disciplineDependencies = addLibs(libs, "discipline", "scalacheck")

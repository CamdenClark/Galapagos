import com.typesafe.sbt.web.Import.WebKeys.webJarsDirectory

import org.nlogo.PlayScrapePlugin.credentials.{ fromCredentialsProfile, fromEnvironmentVariables }

name := "Galapagos"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:_",
  // Scala 2.12.2 produces warnings for unused imports, but Play generates
  // files as part of compilation that have unused imports, so we have to
  // disable these warnings for now.  -JMB July 2017
  "-Xlint:-unused",
  "-Ywarn-value-discard",
  "-Xfatal-warnings"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, org.nlogo.PlayScrapePlugin)

val tortoiseVersion = "1.0-949fa14"

libraryDependencies ++= Seq(
  ehcache,
  filters,
  guice,
  "org.nlogo" % "tortoise" % tortoiseVersion,
  "org.nlogo" % "netlogowebjs" % tortoiseVersion,
  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.1" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0-RC1" % "test"
)

libraryDependencies ++= Seq(
  "org.webjars" % "chosen" % "1.3.0",
  "org.webjars.npm" % "filesaver.js" % "0.1.1",
  "org.webjars.npm" % "mousetrap" % "1.5.3",
  "org.webjars.bower" % "google-caja" % "6005.0.0",
  "org.webjars" % "highcharts" % "5.0.6",
  "org.webjars" % "jquery" % "3.1.1",
  "org.webjars" % "markdown-js" % "0.5.0-1",
  "org.webjars" % "ractive" % "0.7.3",
  "org.webjars" % "codemirror" % "5.13.2",
  "org.webjars.bower" % "github-com-highcharts-export-csv" % "1.4.3"
)

resolvers += bintray.Opts.resolver.repo("netlogo", "TortoiseAux")

resolvers += bintray.Opts.resolver.repo("netlogo", "NetLogoHeadless")

resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/" // Needed for akka-http (for now, at least) --JAB 5/23/17

GalapagosAssets.settings

// Used in Prod
pipelineStages ++= Seq(digest)

fork in Test := false

routesGenerator := InjectedRoutesGenerator

scrapeRoutes ++= Seq(
  "/humans.txt",
  "/info",
  "/whats-new",
  "/model/list.json",
  "/model/statuses.json",
  "/netlogo-engine.js",
  "/netlogo-agentmodel.js",
  "/tortoise-compiler.js",
  "/tortoise-compiler.js.map",
  "/server-error",
  "/not-found",
  "/robots.txt",
  "/standalone",
  "/launch",
  "/web"
  )

scrapeDelay := 120

def isJenkins: Boolean = Option(System.getenv("JENKINS_HOME")).nonEmpty

def jenkinsBranch: String =
  if (Option(System.getenv("CHANGE_TARGET")).isEmpty)
    System.getenv("BRANCH_NAME")
  else
    "PR-" + System.getenv("CHANGE_ID") + "-" + System.getenv("CHANGE_TARGET")

scrapePublishCredential := (Def.settingDyn {
  if (isJenkins)
    Def.setting { fromEnvironmentVariables }
  else
    // Requires setting up a credentials profile, ask Robert for more details
    Def.setting { fromCredentialsProfile("nlw-admin") }
}).value

scrapePublishBucketID := (Def.settingDyn {
  val branchDeploy = Map("master" -> "netlogo-web-prod-content")

  if (isJenkins)
    Def.setting { branchDeploy.get(jenkinsBranch) }
  else
    Def.setting { branchDeploy.get("master") }
}).value

scrapePublishDistributionID := (Def.settingDyn {
  val branchPublish = Map("master" -> "E3AIHWIXSMPCAI")

  if (isJenkins)
    Def.setting { branchPublish.get(jenkinsBranch) }
  else
    Def.setting { branchPublish.get("master") }
}).value

scrapeAbsoluteURL := Some("netlogoweb.org")

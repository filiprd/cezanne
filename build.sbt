ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.3"

ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Wunused:all",
  "-no-indent"
)

ThisBuild / semanticdbEnabled := true
ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val zioVersion       = "2.0.19"
val tapirVersion     = "1.2.6"
val zioConfigVersion = "3.0.7"

libraryDependencies ++= Seq(
  "dev.zio"                       %% "zio-json"                          % "0.4.2",
  "dev.zio"                       %% "zio-config"                        % zioConfigVersion,
  "dev.zio"                       %% "zio-config-magnolia"               % zioConfigVersion,
  "dev.zio"                       %% "zio-config-typesafe"               % zioConfigVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-zio"                         % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"             % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"           % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"            % tapirVersion % "test",
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"                    % tapirVersion,
  "com.softwaremill.sttp.client3" %% "zio"                               % "3.8.8",
  "io.getquill"                   %% "quill-jdbc-zio"                    % "4.7.3",
  "org.postgresql"                 % "postgresql"                        % "42.5.0",
  "com.auth0"                      % "java-jwt"                          % "4.2.1",
  "ch.qos.logback"                 % "logback-classic"                   % "1.4.4",
  "dev.zio"                       %% "zio-test"                          % zioVersion,
  "dev.zio"                       %% "zio-test-junit"                    % zioVersion   % "test",
  "dev.zio"                       %% "zio-test-sbt"                      % zioVersion   % "test",
  "dev.zio"                       %% "zio-test-magnolia"                 % zioVersion   % "test",
  "dev.zio"                       %% "zio-mock"                          % "1.0.0-RC9"  % "test",
  "io.github.scottweaver"         %% "zio-2-0-testcontainers-postgresql" % "0.9.0"
)

lazy val root = (project in file("."))
  .settings(
    name              := "cezanne",
    scalafmtOnCompile := true
  )

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFileInstruction

plugins {
  alias(libs.plugins.kotlin.jvm)
  id("application")
  alias(libs.plugins.dockerJavaApplication)
  alias(libs.plugins.tapmoc)
}

application {
  mainClass.set("org.jraf.resplit.main.MainKt")
}

dependencies {
  // Coroutines
  implementation(libs.kotlinx.coroutines.core)

  // Logging
  implementation(libs.klibnanolog)
  implementation(libs.logback)

  // Server
  implementation(project(":receipt-extractor"))
  implementation(project(":server"))
}

docker {
  javaApplication {
    baseImage.set("eclipse-temurin:25")
    maintainer.set("BoD <BoD@JRAF.org>")
    ports.set(listOf(8042))
    images.add("bodlulu/${rootProject.name.lowercase()}:latest")
    jvmArgs.set(listOf("-Xms16m", "-Xmx128m"))
  }
  registryCredentials {
    username.set(System.getenv("DOCKER_USERNAME"))
    password.set(System.getenv("DOCKER_PASSWORD"))
  }
}

tasks.withType<DockerBuildImage> {
  platform.set("linux/amd64")
}

tasks.withType<Dockerfile> {
  // Install curl
  runCommand("apt-get update")
  runCommand("apt-get install -y curl")

  // Download the OpenTelemetry Java agent
  runCommand("curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar")

  // OpenTelemetry Java agent configuration
  environmentVariable(
    mapOf(
      "JAVA_TOOL_OPTIONS" to "-javaagent:opentelemetry-javaagent.jar",
      "OTEL_SERVICE_NAME" to rootProject.name.lowercase(),
    )
  )

  // Move the COPY instructions to the end
  // See https://github.com/bmuschko/gradle-docker-plugin/issues/1093
  instructions.set(
    instructions.get().sortedBy { instruction ->
      if (instruction.keyword == CopyFileInstruction.KEYWORD) 1 else 0
    }
  )
}

tapmoc {
  java(25)
  kotlin(libs.versions.kotlin.get())
  checkDependencies()
}

// `DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage` to build and push the image

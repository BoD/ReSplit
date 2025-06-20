import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFileInstruction

plugins {
  kotlin("jvm")
  id("application")
  id("com.bmuschko.docker-java-application")
}

kotlin {
  jvmToolchain(11)
}

application {
  mainClass.set("org.jraf.resplit.main.MainKt")
}

dependencies {
  // Coroutines
  implementation(KotlinX.coroutines.core)

  // Logging
  implementation("org.slf4j:slf4j-api:_")
  implementation("org.slf4j:slf4j-simple:_")
  implementation("org.jraf:klibnanolog:_")

  // Server
  implementation(project(":receipt-extractor"))
  implementation(project(":server"))
}

docker {
  javaApplication {
    // Use OpenJ9 instead of the default one
    baseImage.set("adoptopenjdk/openjdk11-openj9:x86_64-ubuntu-jre-11.0.26_4_openj9-0.49.0")
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
  // Move the COPY instructions to the end
  // See https://github.com/bmuschko/gradle-docker-plugin/issues/1093
  instructions.set(
    instructions.get().sortedBy { instruction ->
      if (instruction.keyword == CopyFileInstruction.KEYWORD) 1 else 0
    }
  )
}

// `DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage` to build and push the image

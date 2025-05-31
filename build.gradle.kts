plugins {
  kotlin("multiplatform").apply(false)
  kotlin("jvm").apply(false)
  kotlin("plugin.serialization").apply(false)
  id("com.bmuschko.docker-java-application").apply(false)
}

group = "org.jraf.resplit"
version = "1.0.0"

// `./gradlew refreshVersions` to update dependencies
// `./gradlew distZip` to create a zip distribution
// `DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage` to build and push the image

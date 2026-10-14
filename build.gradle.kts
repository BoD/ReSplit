plugins {
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.jvm).apply(false)
  alias(libs.plugins.kotlin.serialization).apply(false)
  alias(libs.plugins.kotlin.compose).apply(false)
  alias(libs.plugins.compose).apply(false)
  alias(libs.plugins.dockerJavaApplication).apply(false)
  alias(libs.plugins.tapmoc).apply(false)
}

group = "org.jraf.resplit"
version = "1.0.0"

// `./gradlew refreshVersions` to update dependencies
// `./gradlew distZip` to create a zip distribution
// `DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage` to build and push the image

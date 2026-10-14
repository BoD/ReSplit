plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm()
  js {
    browser()
    compilerOptions {
      target.set("es2015")
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        // Logging
        implementation(libs.klibnanolog)

        // Json
        implementation(libs.kotlinx.serialization.json)
      }
    }

    jvmMain {
      dependencies {
        // Jackson annotations
        implementation(libs.jackson.annotations)
      }
    }
  }
}

tapmoc {
  java(25)
  kotlin(libs.versions.kotlin.get())
  checkDependencies()
}


plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(25)
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

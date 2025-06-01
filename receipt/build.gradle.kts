plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

kotlin {
  jvmToolchain(11)
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
        implementation("org.jraf:klibnanolog:_")

        // Json
        implementation(KotlinX.serialization.json)
      }
    }

    jvmMain {
      dependencies {
        // Jackson annotations
        implementation("com.fasterxml.jackson.core:jackson-annotations:_")
      }
    }
  }
}

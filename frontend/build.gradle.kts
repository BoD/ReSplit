plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  kotlin("plugin.compose")
}

kotlin {
  js {
    browser()
    binaries.executable()
    compilerOptions {
      target.set("es2015")
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        // Logging
        implementation("org.jraf:klibnanolog:_")

        // Compose
        implementation(compose.html.core)
        implementation(compose.runtime)

        // BigNum
        implementation("com.ionspin.kotlin:bignum:0.3.10")

        // Receipt
        implementation(project(":receipt"))
      }
    }
  }
}

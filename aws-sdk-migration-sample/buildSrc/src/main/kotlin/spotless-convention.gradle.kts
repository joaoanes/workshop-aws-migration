// buildSrc/src/main/kotlin/spotless-convention.gradle.kts
plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        target("*.gradle.kts", "src/**/*.kt")
        ktlint()
    }
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
}

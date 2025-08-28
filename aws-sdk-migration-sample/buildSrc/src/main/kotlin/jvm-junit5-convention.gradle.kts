// buildSrc/src/main/kotlin/jvm-junit5-convention.gradle.kts
plugins {
    `java-library`
}

dependencies {
    val junitVersion = "5.10.2"
    val assertjVersion = "3.25.3"

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.assertj:assertj-core:${assertjVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

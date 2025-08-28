plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("jvm-junit5-convention")
    id("spotless-convention")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("lambda-handler")
    archiveClassifier.set("all")
}

dependencies {
    implementation(project(":modules:core"))
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("com.google.dagger:dagger:2.51")
    annotationProcessor("com.google.dagger:dagger-compiler:2.51")

    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.740"))
    implementation("com.amazonaws:aws-java-sdk-sqs")
    implementation("com.amazonaws:aws-java-sdk-s3")

    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.1.6")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:localstack")
}

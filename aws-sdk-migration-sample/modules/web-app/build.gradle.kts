plugins {
    `java-library`
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.5"
    id("jvm-junit5-convention")
    id("spotless-convention")
}

dependencies {
    implementation(project(":modules:core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("com.google.dagger:dagger:2.51")
    annotationProcessor("com.google.dagger:dagger-compiler:2.51")

    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.740"))
    implementation("com.amazonaws:aws-java-sdk-sqs")
    implementation("com.amazonaws:aws-java-sdk-s3")

    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:localstack")
    testImplementation(project(":modules:lambda"))
    testImplementation("org.junit-pioneer:junit-pioneer:2.2.0")
    testImplementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    testImplementation("com.amazonaws:aws-lambda-java-core:1.2.2")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom("org.junit:junit-bom:5.10.2")
    }
}

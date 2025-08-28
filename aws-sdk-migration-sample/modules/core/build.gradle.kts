plugins {
    `java-library`
    id("jvm-junit5-convention")
    id("spotless-convention")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
}

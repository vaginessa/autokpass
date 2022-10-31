import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.31")
    id("org.jetbrains.kotlin.plugin.spring").version("1.5.31")
    id("org.springframework.boot").version("2.2.0.RELEASE")
    jacoco
}

apply(plugin = "io.spring.dependency-management")

val appVersion = "0.7.0"

group = "com.github.ai.autokpass"
version = appVersion

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.jacocoTestReport {
    reports {
        val coverageDir = File("$buildDir/reports/coverage")
        csv.required.set(true)
        csv.outputLocation.set(File(coverageDir, "coverage.csv"))
        html.required.set(true)
        html.outputLocation.set(coverageDir)
    }

    classDirectories.setFrom(classDirectories.files.map {
        fileTree(it).matching {
            exclude("com/github/ai/autokpass/di/**")
        }
    })

    dependsOn(allprojects.map { it.tasks.named<Test>("test") })
}

tasks.bootJar {
    archiveFileName.set("autokpass.jar")
}

tasks.classes {
    dependsOn("createPropertyFileWithVersion")
}

tasks.register("createPropertyFileWithVersion") {
    doLast {
        val propertyName = "version"
        val propsFile = File("$projectDir/src/main/resources/version.properties")
        val props = Properties()

        if (propsFile.exists()) {
            props.load(FileInputStream(propsFile))
        }

        if (props[propertyName] != appVersion) {
            project.logger.lifecycle("Updating file: version.properties")
            props[propertyName] = appVersion
            props.store(BufferedWriter(FileWriter(propsFile)), "File is generated by Gradle")
        }
    }
}

dependencies {
    // testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.3")
    testImplementation("io.mockk:mockk:1.12.3")

    implementation("io.insert-koin:koin-core:3.1.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")
    implementation("org.buildobjects:jproc:2.8.0")
    implementation("de.gesundkrank.fzf4j:fzf4j:0.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("com.github.anvell:kotpass:0.4.9")
}
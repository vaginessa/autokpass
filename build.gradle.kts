import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.7.20")
    id("org.jetbrains.compose") version "1.2.1"
    jacoco
}

val appVersion = "1.0.0"

group = "com.github.ai.autokpass"
version = appVersion

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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

compose.desktop {
    application {
        mainClass = "com.github.ai.autokpass.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("java.instrument", "jdk.unsupported", "java.naming")
            packageName = "autokpass-compose"
            packageVersion = appVersion
        }
    }
}

dependencies {
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.3")
    testImplementation("io.mockk:mockk:1.12.3")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")

    // Compose
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)

    // Navigation
    implementation("com.arkivanov.decompose:decompose:0.8.0")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:0.8.0")

    // DI
    implementation("io.insert-koin:koin-core:3.1.5")

    // Logging
    implementation("ch.qos.logback:logback-core:1.3.5")
    implementation("ch.qos.logback:logback-classic:1.3.5")

    // Keepass
    implementation("com.github.anvell:kotpass:0.4.9")

    // Other
    implementation("org.buildobjects:jproc:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("com.github.aivanovski:fzf4j:0.2.1")
}
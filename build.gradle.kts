@file:Suppress("SpellCheckingInspection", "PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val KT_VER = "1.6.21"

group = "me.dkim19375"
version = "1.1.0"

val basePackage = "me.dkim19375.${project.name.toLowerCase()}.libs"
val fileName = tasks.shadowJar.get().archiveFileName.get()

val VERSION = "11"

tasks.withType<JavaCompile> {
    sourceCompatibility = VERSION
    targetCompatibility = VERSION
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = VERSION
    }
}

license {
    header.set(rootProject.resources.text.fromFile("HEADER"))
    include("**/*.kt")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.triumphteam.dev/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/releases/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(fileTree("libs"))

    compileOnly("com.destroystokyo.paper:paper-api:1.16.4-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.10")

    implementation("net.kyori:adventure-api:4.10.1")
    implementation("net.kyori:adventure-extra-kotlin:4.11.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.0")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("me.mattstudios:triumph-config:1.0.5-SNAPSHOT")
    implementation("net.kyori:adventure-text-serializer-plain:4.11.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.11.0")
    implementation("io.github.dkim19375:dkim-bukkit-core:3.3.39") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KT_VER") {
        exclude(module = "annotations")
    }
}

val server = "1.16"
val servers = setOf(
    "1.8",
    "1.16",
    "1.17",
    "1.18"
)

tasks {
    processResources {
        outputs.upToDateWhen { false }
        expand("pluginVersion" to project.version)
    }

    create("removeBuildJars") {
        doLast {
            File(project.rootDir, "build/libs").deleteRecursively()
        }
    }

    val copyFile by registering {
        doLast {
            val jar = shadowJar.get().archiveFile.get().asFile
            val pluginFolder = file(rootDir).resolve("../.TestServers/${server}/plugins")
            if (pluginFolder.exists()) {
                jar.copyTo(File(pluginFolder, shadowJar.get().archiveFileName.get()), true)
            }
        }
    }

    create("deleteAll") {
        doLast {
            for (deleteServer in servers) {
                for (file in File("../.TestServers/${deleteServer}/plugins").listFiles() ?: emptyArray()) {
                    if (file.name.startsWith(shadowJar.get().archiveBaseName.get())) {
                        file.delete()
                    }
                }
            }
        }
    }

    val relocations = setOf(
        "kotlin",
        "kotlinx",
        "reactor",
        "net.kyori",
        "org.yaml.snakeyaml",
        "org.reactivestreams",
        "me.mattstudios.config",
        "me.dkim19375.dkimcore",
        "org.jetbrains.annotations",
        "me.dkim19375.dkimbukkitcore",
        "org.intellij.lang.annotations",
    )

    shadowJar {
        relocations.forEach { name ->
            relocate(name, "${basePackage}.$name")
        }
        exclude("DebugProbesKt.bin")
        mergeServiceFiles()
        finalizedBy(copyFile)
    }
}

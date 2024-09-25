import org.slf4j.event.Level

val loader = "kotlinforforge"
val modId = "binarycraft"
val modName = "BinaryCraft"
val modLicense = "MIT license"
val modGroupId = "eu.withoutaname.mod"
val modAuthors = "WithoutAName"
val modDescription = """
        Digital circuits in Minecraft
    """.trimIndent()

plugins {
    java
    `maven-publish`
    alias(libs.plugins.neo.moddev)
    alias(libs.plugins.kotlin.jvm)
}

version = libs.versions.mod.get()
group = modGroupId


repositories {
    mavenCentral()
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

base {
    archivesName.set(modId)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

neoForge {
    version = libs.versions.neo.asProvider().get()

    parchment {
        minecraftVersion.set(libs.versions.parchment.minecraft.get())
        mappingsVersion.set(libs.versions.parchment.mappings.get())
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")

            logLevel.set(Level.DEBUG)
        }

        create("client") {
            client()
            gameDirectory.set(file("run/client"))
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
            gameDirectory.set(file("run/server"))
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("gameTestServer") {
            type.set("gameTestServer")
            gameDirectory.set(file("run/gameTestServer"))
            systemProperty("forge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            data()
            gameDirectory.set(file("run/data"))
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        }
    }
}

sourceSets.main.get().resources {
    srcDir("src/generated/resources")
}

dependencies {
    implementation(libs.kotlinforforge)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}

val generateModMetadataTask = tasks.register("generateModMetadata", ProcessResources::class.java) {
    val replaceProperties = mapOf(
        "minecraft_version" to libs.versions.minecraft.asProvider().get(),
        "minecraft_version_range" to libs.versions.minecraft.range.get(),
        "neo_version" to libs.versions.neo.asProvider().get(),
        "neo_version_range" to libs.versions.neo.range.get(),
        "loader" to loader,
        "loader_version_range" to libs.versions.kotlinForForge.range.get(),
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_version" to libs.versions.mod.get(),
        "mod_authors" to modAuthors,
        "mod_description" to modDescription,
        "pack_format_number" to libs.versions.packFormat.get()
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

sourceSets.main.get().resources.srcDir(generateModMetadataTask)

tasks.processResources.get().dependsOn(generateModMetadataTask)

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifact(tasks.jar)
        }
    }
    repositories {
        maven("file://$projectDir/mcmodsrepo")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

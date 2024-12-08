
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("fabric-loom") version "1.9-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val graal_version: String by project
val bytebuddy_version: String by project
val classtransform_version: String by project
val swc4j_version: String by project
val mapping_io_version: String by project
val strikt_version: String by project
val semverVersion: String by project

val mod_id: String by project
version = "${project.property("mod_version")}"
group = project.property("maven_group") as String

base {
    archivesName.set("${project.property("mod_id")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (name.contains("test", ignoreCase = true)) {
        options.release.set(21)
    } else {
        options.release.set(8)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    if (name.contains("test", ignoreCase = true)) {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    } else {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Put mixins into kotlin folder
sourceSets.getByName("main").java.setSrcDirs(listOf("src/main/kotlin"))

sourceSets {
    val stub by creating {
        java {
            setSrcDirs(listOf("src/stub/java"))
        }
    }
    main {
        java {
            srcDirs("src/main/java")
        }
        compileClasspath += sourceSets["stub"].output
    }
}

repositories {
    maven {
        name = "neoforged"
        url = uri("https://maven.neoforged.net/releases")
    }
}

dependencies {
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")

    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")
    modImplementation(files("libs/jsmacros-1.21.4-2.0.0-fabric.jar"))

    implementation("org.graalvm.sdk:graal-sdk:$graal_version")
    implementation("org.graalvm.js:js:$graal_version")
    implementation("com.caoccao.javet:swc4j:$swc4j_version")
    implementation("net.fabricmc:mapping-io:$mapping_io_version")
    implementation("io.github.z4kn4fein:semver:$semverVersion")

    implementation("net.bytebuddy:byte-buddy-agent:$bytebuddy_version")
    implementation("net.lenni0451.classtransform:core:$classtransform_version")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.1")
    testImplementation("io.strikt:strikt-core:$strikt_version")
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
    filesMatching("META-INF/mods.toml") {
        expand(project.properties)
    }
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(project.properties)
    }
}

tasks.shadowJar {
    dependencies {
        include(dependency("com.caoccao.javet:swc4j:$swc4j_version"))
        include(dependency("net.fabricmc:mapping-io:$mapping_io_version"))
        include(dependency("net.bytebuddy:byte-buddy-agent:$bytebuddy_version"))
        include(dependency("net.lenni0451.classtransform:core:$classtransform_version"))
        include(dependency("io.github.z4kn4fein:semver-jvm:$semverVersion"))
    }
    exclude("win32-x86/**", "win32-x86-64/**", "mappings/**")
}

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
}

tasks.build {
    doLast {
        tasks.shadowJar.get().archiveFile.get().asFile.delete()
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "MixinConfigs" to "graalbridge.mixins.json"
        )
    }
    from(sourceSets.main.get().output)
}

kotlin {
    compilerOptions.freeCompilerArgs.add("-Xmulti-dollar-interpolation")
}

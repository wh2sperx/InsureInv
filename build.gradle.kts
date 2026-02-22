import java.time.Instant

plugins {
    kotlin("jvm") version "2.3.10"
    id("com.gradleup.shadow") version "9.3.1"
}

// ---------------------------------------------------------------------------
// Git properties generation (tương tự git-commit-id-maven-plugin)
// ---------------------------------------------------------------------------
val generateGitProperties by tasks.registering {
    group = "build"
    description = "Generates git.properties file with commit information"

    val outputFile = layout.buildDirectory.file("generated/resources/git/git.properties")

    // Input tracking để Gradle biết khi nào cần regenerate
    inputs.property("git.head", providers.exec {
        commandLine("git", "rev-parse", "HEAD")
        isIgnoreExitValue = true
    }.standardOutput.asText.map { it.trim() }.orElse("unknown"))

    outputs.file(outputFile)

    doLast {
        fun runGit(command: String): String = providers.exec {
            commandLine("git", *command.split(" ").toTypedArray())
            isIgnoreExitValue = true
        }.standardOutput.asText.map { it.trim() }.getOrElse("unknown")

        val props = mapOf(
            "git.commit.id" to runGit("rev-parse HEAD"),
            "git.commit.id.abbrev" to runGit("rev-parse --short HEAD"),
            "git.commit.message.short" to runGit("log -1 --pretty=%s"),
            "git.commit.time" to runGit("log -1 --pretty=%cI"),
            "git.branch" to runGit("rev-parse --abbrev-ref HEAD"),
            "git.build.time" to Instant.now().toString(),
            "git.dirty" to (runGit("status --porcelain").isNotBlank()).toString()
        )

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(props.entries.joinToString("\n") { "${it.key}=${it.value}" })
        }
    }
}

// Define gitCommitShort at top level for reuse
val gitCommitShort = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim() }.getOrElse("unknown")

// Thêm generated resources vào sourceSets
sourceSets {
    main {
        resources {
            srcDir(layout.buildDirectory.dir("generated/resources/git"))
        }
    }
}

// ---------------------------------------------------------------------------
// Repositories
// ---------------------------------------------------------------------------
repositories {                                         // local hyticallib-i18n jars
    maven("https://repo.papermc.io/repository/maven-public/")                // Paper API
    maven("https://jitpack.io")                                              // VaultAPI
    maven("https://repo.helpch.at/releases")                                 // PlaceholderAPI
    maven("https://repo.tcoded.com/releases")                                // FoliaLib
    maven("https://repo.rosewooddev.io/repository/public/")                  // PlayerPoints
    mavenCentral()
}

// ---------------------------------------------------------------------------
// Dependencies
// ---------------------------------------------------------------------------
val coroutinesVersion: String by project

dependencies {
    // --- provided by server / other plugins → compileOnly ---
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.black_ixx:playerpoints:3.3.3")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("com.mysql:mysql-connector-j:8.3.0")
    compileOnly("org.xerial:sqlite-jdbc:3.45.1.0")
    compileOnly("com.google.code.gson:gson:2.10.1")

    // --- loaded via Paper Library Loader → compileOnly ---
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // --- shaded into JAR → implementation ---
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("com.tcoded:FoliaLib:0.4.3")
    implementation("com.github.qhuyluvyou.hyticallib-i18n:hyticallib-i18n-core:1.0.1")
    implementation("com.github.qhuyluvyou.hyticallib-i18n:hyticallib-i18n-bukkit:1.0.1")
}

// ---------------------------------------------------------------------------
// Kotlin
// ---------------------------------------------------------------------------
kotlin {
    jvmToolchain(21)
}

// ---------------------------------------------------------------------------
// Resource filtering  (plugin.yml token replacement)
// ---------------------------------------------------------------------------
tasks.processResources {
    dependsOn(generateGitProperties) // Đảm bảo git.properties được generate trước

    val props = mapOf(
        "name"      to project.name,
        "version"   to project.version.toString(),
        "gitCommit" to gitCommitShort,
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// ---------------------------------------------------------------------------
// Shadow JAR
// ---------------------------------------------------------------------------
tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("${project.name}-${project.version}+${gitCommitShort}.jar")

    dependencies {
        include(dependency("com.tcoded:FoliaLib"))
        include(dependency("org.bstats:bstats-bukkit"))
        include(dependency("org.bstats:bstats-base"))
        include(dependency("com.github.HyticMC.hyticallib-i18n:hyticallib-i18n-core"))
        include(dependency("com.github.HyticMC.hyticallib-i18n:hyticallib-i18n-bukkit"))
    }

    relocate("com.tcoded.folialib", "dev.hytical.insureinv.libs.folialib")
    relocate("org.bstats", "dev.hytical.insureinv.libs.bstats")
    
    relocate("dev.hytical.i18n", "dev.hytical.insureinv.libs.i18n") {
        // Include tất cả subpackages
        include("dev.hytical.i18n.**")
    }

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/versions/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/proguard/**")
}

// shadowJar replaces the default jar
tasks.build {
    dependsOn(tasks.shadowJar)
}

// ---------------------------------------------------------------------------
// Tasks
// ---------------------------------------------------------------------------
tasks.register("printGitInfo") {
    group = "help"
    description = "Prints current git information"
    doLast {
        println("\n=== Git Information ===")
        file(layout.buildDirectory.file("generated/resources/git/git.properties").get().asFile)
            .takeIf { it.exists() }
            ?.readLines()
            ?.forEach { println(it) }
            ?: println("Git properties not generated yet. Run 'build' first.")
    }
}

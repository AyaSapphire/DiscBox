plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fr.skytasul"
version = "1.20.14"

repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")

    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.org/repository/maven-public")
    // Jitpack for GitHub Only dependencies
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.github.koca2000:NoteBlockAPI:1.6.2")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.tchristofferson:ConfigUpdater:2.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("project" to mapOf("version" to version))
    }
}

tasks.shadowJar {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    archiveVersion.set(version.toString())

    relocate("org.bstats", "fr.skytasul.music.utils.bstats")
    relocate("com.tchristofferson.configupdater", "fr.skytasul.music.utils.configupdater")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

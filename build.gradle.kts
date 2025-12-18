plugins {
    id("fabric-loom") version "1.14-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
}

val modVersion = properties["mod_version"] as String
val minecraftVersion = properties["minecraft_version"] as String
val loaderVersion = properties["loader_version"] as String
val mavenGroup = properties["maven_group"] as String
val fabricVersion = properties["fabric_version"] as String
val archivesBaseName = properties["archives_base_name"] as String
val fabricKotlinVersion = properties["fabric_kotlin_version"] as String

version = modVersion
group = mavenGroup

repositories {
    mavenCentral()
    maven { url = uri("https://maven.fabricmc.net/") }

    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")
}

tasks.named<Copy>("processResources") {
    val props = mapOf("version" to version,
        "minecraft_version" to minecraftVersion,
        "loader_version" to loaderVersion)

    inputs.properties(props)

    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

val targetJavaVersion = 21

tasks.named<JavaCompile>("compileJava") {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

kotlin {
    // for some reason, this is needed to build with gradle 8.8
    // and java 21, and `jvmToolchain(21)` does not work.
    jvmToolchain(21)
}

tasks.named<Jar>("jar") {
    from("LICENSE") {
        rename { "${it}_${archivesBaseName}"}
    }
}

loom {
    accessWidenerPath = file("src/main/resources/chunkloaderpersistence.accesswidener")
}

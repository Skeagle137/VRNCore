plugins {
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.6"
}

dependencies {
    implementation(project(":api"))
    implementation(project(":nms-1_21_R1"))
    implementation(project(":nms-1_21_R2"))
    implementation(project(":nms-1_21_R3"))
    implementation(project(":nms-1_21_R4"))
    implementation(project(":nms-1_21_R5"))

    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.LeonMangler:SuperVanish:6.2.18-3")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.0")
    compileOnly("org.xerial:sqlite-jdbc:3.47.0.0")
}

tasks {

    shadowJar {
        archiveClassifier.set("")
    }

    register("updatePlugins") {
        dependsOn("jar")
        doLast {
            copy {
                from("build/libs/")
                project.property("pluginsPath")?.let { into(it) }
                rename { "${rootProject.name} ${version}.jar" }
            }
        }
    }

    withType<ProcessResources> {
        val props = mapOf("version" to version)
        inputs.properties(props)
            filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    withType<JavaCompile> {
        options.compilerArgs.addLast("-parameters")
    }

    build {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "net.skeagle"
            artifactId = rootProject.name
            from(components["java"])
        }
    }
}
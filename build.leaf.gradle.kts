import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("me.decce.transformingbase.gradle.transformingbase-common-conventions")
    id("com.gradleup.shadow")
}

val modId = property("modid").toString()
val workshopItemName = property("mod_name").toString()

repositories {
    mavenCentral()
    maven("https://maven.aoqia.dev/releases/") {
        content { includeGroup("dev.aoqia.leaf") }
    }
    maven("https://maven.fabricmc.net/") {
        content { includeGroup("net.fabricmc") }
    }
}

sourceSets {
    named("main") {
        resources.srcDir(rootProject.file("src/leaf/resources"))
    }
    named("service") {
        java.srcDir(rootProject.file("src/leaf/java"))
        resources.exclude("META-INF/services/**")
    }
}

dependencies {
    compileOnly("dev.aoqia.leaf:loader:${property("leaf_loader_version")}@jar")
    compileOnly("net.fabricmc:sponge-mixin:0.17.2+mixin.0.8.7")
    compileOnly("org.ow2.asm:asm-analysis:9.10.1")
    compileOnly("org.apache.logging.log4j:log4j-api:2.19.0")
}

tasks {
    named<Jar>("jar") {
        archiveClassifier = "slim"
    }

    val leafJar = named<ShadowJar>("shadowJar") {
        archiveClassifier = ""
    }

    assemble {
        dependsOn(leafJar)
    }

    val workshop = register<Sync>("buildWorkshop") {
        group = "build"
        into(layout.buildDirectory.dir("distributions/$workshopItemName"))
        from(rootProject.file("src/leaf/workshop"))

        into("Contents/mods/$modId/42") {
            from(rootProject.file("src/leaf/mod.info"))
            from(leafJar.flatMap { it.archiveFile }) {
                into("media/java")
            }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        dependsOn(workshop)
        from(leafJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs"))
    }
}

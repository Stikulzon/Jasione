import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("me.decce.transformingbase.gradle.transformingbase-common-conventions")
    id("dev.aoqia.leaf.loom") version "1.1.2"
}

val modId = property("modid").toString()
val workshopItemName = property("mod_name").toString()
val modResources = rootProject.layout.projectDirectory.dir("src/mod-src/resources")
val workshopFiles = modResources.dir("workshop")

val service = sourceSets.named("service") {
    resources.exclude("META-INF/services/**")
}

sourceSets.main {
    runtimeClasspath += files(service.map { it.output })
}

dependencies {
    zomboid("com.theindiestone:zomboid:${property("deps.zomboid")}")
    implementation("dev.aoqia.leaf:loader:${property("leaf_loader_version")}")
    compileOnly("net.fabricmc:sponge-mixin:0.17.2+mixin.0.8.7")
    compileOnly("org.ow2.asm:asm-analysis:9.10.1")
    compileOnly("org.apache.logging.log4j:log4j-api:2.19.0")
}

val leafJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier = ""
    val packagedEntrypoint = "me.decce.$modId.service.leaf.LeafPreLaunchEntrypoint"
    inputs.property("leafPreLaunchEntrypoint", packagedEntrypoint)
    filesMatching("leaf.mod.json") {
        filter { line: String ->
            line.replace("me.decce.transformingbase.service.leaf.LeafPreLaunchEntrypoint", packagedEntrypoint)
        }
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

loom {
    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(service.get())
        }
    }
    decompilers {
        named("vineflower") {
            options.put("included-classes", "zombie.*")
        }
    }
}

tasks {
    processResources {
        from(modResources) {
            include("leaf.mod.json")
        }
    }

    withType<JavaCompile>().configureEach {
        options.release.set(25)
    }

    named<Jar>("jar") {
        archiveClassifier = "slim"
    }

    assemble {
        dependsOn(leafJar)
    }

    val workshop = register<Sync>("buildWorkshop") {
        group = "build"
        into(layout.buildDirectory.dir("distributions/$workshopItemName"))
        from(workshopFiles)

        into("Contents/mods/$modId/42") {
            from(modResources.file("mod.info"))
            from(workshopFiles.file("preview.png"))
            from(leafJar.flatMap { it.archiveFile }) {
                into("media/java")
            }
        }
    }

    val workshopZip = register<Zip>("buildWorkshopZip") {
        group = "build"
        description = "Packages the staged Workshop item as a ZIP."
        archiveClassifier = "workshop"
        destinationDirectory = layout.buildDirectory.dir("distributions")
        from(workshop) {
            into(workshopItemName)
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(leafJar.flatMap { it.archiveFile })
        from(workshopZip.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs"))
    }
}

plugins {
	id("net.fabricmc.fabric-loom")
	`maven-publish`
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.

	maven("https://maven.terraformersmc.com/") {
		name = "Terraformers"
	}

	maven("https://maven.isxander.dev/releases") {
		name = "Xander Maven"
	}
}

loom {
	splitEnvironmentSourceSets()

	mods {
		register("autoclutch") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets.getByName("client"))
		}
	}
}

dependencies {
// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")

	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")

	// Mod dependencies
	implementation("com.terraformersmc:modmenu:${providers.gradleProperty("modmenu_version").get()}")
	implementation("dev.isxander:yet-another-config-lib:${providers.gradleProperty("yacl_version").get()}")
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	inputs.property("projectName", project.name)

	from("LICENSE") {
		rename { "${it}_${project.name}" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
// YACL derives every config-screen label from the field name and the @AutoGen group/category, but
// the English text itself has to live in en_us.json. A missing key only shows up as a raw key
// string when the screen is opened, so fail the build instead.
val checkConfigLangKeys by tasks.registering {
	val configSrc = file("src/client/java/net/honeyberries/config/AutoClutchConfig.java")
	val langFile = file("src/main/resources/assets/autoclutch/lang/en_us.json")
	inputs.files(configSrc, langFile)
	doLast {
		val src = configSrc.readText()
		val lang = langFile.readText()
		val prefix = "yacl3.config.autoclutch:config."
		val categories = mapOf("GENERAL" to "general")
		val fieldRegex = Regex("""@AutoGen\(category\s*=\s*(\w+),\s*group\s*=\s*"(\w+)"\)[^;]*?public\s+\w+\s+(\w+)\s*=""")
		val required = linkedSetOf<String>()
		val fields = mutableSetOf<String>()
		for (m in fieldRegex.findAll(src)) {
			val cat = categories[m.groupValues[1]] ?: m.groupValues[1].lowercase()
			required += "${prefix}category.$cat"
			required += "${prefix}category.$cat.group.${m.groupValues[2]}"
			required += "${prefix}category.$cat.group.${m.groupValues[2]}.desc"
			required += "$prefix${m.groupValues[3]}"
			required += "$prefix${m.groupValues[3]}.desc"
			fields += m.groupValues[3]
		}
		required += "${prefix.removeSuffix(".")}.title"
		val present = Regex("\"(yacl3\\.config\\.autoclutch:config\\.[^\"]+)\"\\s*:").findAll(lang).map { it.groupValues[1] }.toSet()
		val missing = required - present
		val stale = present.filter { key ->
			val rest = key.removePrefix(prefix).removeSuffix(".desc")
			!rest.startsWith("category.") && rest != "title" && rest !in fields
		}
		if (fields.isEmpty()) throw GradleException("checkConfigLangKeys found no @AutoGen fields; the regex is out of date")
		if (missing.isNotEmpty() || stale.isNotEmpty()) {
			throw GradleException("en_us.json is out of sync with AutoClutchConfig.\n  missing: $missing\n  stale: $stale")
		}
	}
}
tasks.named("check") { dependsOn(checkConfigLangKeys) }

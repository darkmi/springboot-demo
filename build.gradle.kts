allprojects {
	group = "com.darkmi"
	version = "0.0.1-SNAPSHOT"
}

subprojects {
	apply(plugin = "java")

	the<JavaPluginExtension>().toolchain.languageVersion.set(JavaLanguageVersion.of(21))

	repositories {
		mavenCentral()
	}
}

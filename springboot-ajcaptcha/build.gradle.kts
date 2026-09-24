plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.darkmi"
version = "0.0.1-SNAPSHOT"
description = "springboot-ajcaptcha"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Web 支持：内嵌 Tomcat + Spring MVC（Spring Boot 4.x 中由原 spring-boot-starter-web 更名而来）
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	// AJ-Captcha 行为验证码（anji-plus）：滑动拼图 / 文字点选
	// 官方 starter 基于 Spring Boot 2.x，其传递依赖 spring-boot-autoconfigure 需排除，
	// 由本项目自身的 Spring Boot 4.1.1 依赖提供
	implementation("com.anji-plus:spring-boot-starter-captcha:1.3.0") {
		exclude(group = "org.springframework.boot", module = "spring-boot-autoconfigure")
	}
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.bootRun {
	// 子 JVM 显式使用 UTF-8 输出，配合 org.gradle.jvmargs 保证 Gradle 链路中文日志不乱码
	jvmArgs("-Dfile.encoding=UTF-8")
}

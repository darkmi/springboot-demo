plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.darkmi"
version = "0.0.1-SNAPSHOT"
description = "springboot-ajcaptcha-plus"

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
	// AJ-Captcha 增强分支 captcha-plus（xingyuv 维护，芋道源码同款依赖）
	// 原生支持 Spring Boot 3+：jakarta 命名空间 + AutoConfiguration.imports 自动装配，无需手动排除依赖
	implementation("com.xingyuv:spring-boot-starter-captcha-plus:2.0.3")
	// Redis 缓存：生产环境将 aj.captcha.cache-type 切换为 redis 时使用
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
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

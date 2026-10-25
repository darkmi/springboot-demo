plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.darkmi"
version = "0.0.1-SNAPSHOT"
description = "springboot-security"

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
	// Thymeleaf 服务端模板：登录页与后台页面
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	// JDBC 支持：JdbcTemplate + HikariCP 连接池
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	// Spring Security：表单登录 + URL 级权限拦截（Boot 4.1.1 对应 Spring Security 7.x）
	implementation("org.springframework.boot:spring-boot-starter-security")
	// SQLite 驱动（文件数据库，免安装）
	runtimeOnly("org.xerial:sqlite-jdbc:3.47.2.0")

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

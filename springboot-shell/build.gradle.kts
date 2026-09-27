description = "springboot-shell"

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

// 每个示例都是独立的可运行程序（普通 main 方法，无需 Spring 容器），
// 注册对应的 JavaExec 任务，方便命令行直接运行，如：
//   ./gradlew :springboot-shell:runPingWindows
val demoMainClasses = mapOf(
	"runRuntimeExecWindows" to "com.darkmi.springboot.shell.windows.RuntimeExecDemo",
	"runProcessBuilderWindows" to "com.darkmi.springboot.shell.windows.ProcessBuilderDemo",
	"runPingWindows" to "com.darkmi.springboot.shell.windows.PingDemo",
	"runHostWindows" to "com.darkmi.springboot.shell.windows.HostDemo",
	"runRuntimeExecLinux" to "com.darkmi.springboot.shell.linux.RuntimeExecDemo",
	"runProcessBuilderLinux" to "com.darkmi.springboot.shell.linux.ProcessBuilderDemo",
	"runPingLinux" to "com.darkmi.springboot.shell.linux.PingDemo",
	"runHostLinux" to "com.darkmi.springboot.shell.linux.HostDemo",
)

demoMainClasses.forEach { (taskName, mainClassName) ->
	tasks.register<JavaExec>(taskName) {
		group = "demo"
		description = "运行 $mainClassName"
		mainClass.set(mainClassName)
		classpath = sourceSets["main"].runtimeClasspath
		// 子 JVM 显式使用 UTF-8 输出，保证中文日志不乱码（与 ajcaptcha 模块的 bootRun 一致）
		jvmArgs("-Dfile.encoding=UTF-8")
		// 支持给程序传运行参数，如（PingDemo / HostDemo 可用参数覆盖默认域名）：
		//   ./gradlew :springboot-shell:runPingWindows -PrunArgs=baidu.com
		if (project.hasProperty("runArgs")) {
			args((project.property("runArgs") as String).split(" "))
		}
	}
}

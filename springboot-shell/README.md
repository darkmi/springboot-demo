# springboot-shell

笔记《如何在 Java 中执行 Shell 命令》（[mkyong: how-to-execute-shell-command-from-java](https://mkyong.com/java/how-to-execute-shell-command-from-java/)）的示例代码迁移。

涉及两个 JDK 自带的 API：`Runtime.getRuntime().exec` 与 `ProcessBuilder`，无需引入任何第三方依赖。
示例均为普通 `main` 方法的可运行程序，**按操作系统拆分**为 `windows` / `linux` 两个包（Linux 包同时适用于 macOS），无需启动 Spring 容器。

## 程序清单

| 操作系统 | 程序 | 对应笔记章节 | 说明 |
| -------- | ---- | ------------ | ---- |
| Windows | `com.darkmi.springboot.shell.windows.RuntimeExecDemo` | Runtime.getRuntime().exec() 示例 | `cmd /c` 执行命令；注释中含运行 bat 文件的写法 |
| Windows | `com.darkmi.springboot.shell.windows.ProcessBuilderDemo` | ProcessBuilder 示例 | `cmd.exe /c` + `directory()` 指定工作目录 |
| Windows | `com.darkmi.springboot.shell.windows.PingDemo` | PING 示例 | `ping -n 3 <域名>`，可用运行参数覆盖域名 |
| Windows | `com.darkmi.springboot.shell.windows.HostDemo` | HOST 示例 | 借助 WSL 执行 `host -t a <域名>` + 正则提取 IPv4，需已安装 WSL 且发行版内装有 dnsutils / bind-utils |
| Linux/macOS | `com.darkmi.springboot.shell.linux.RuntimeExecDemo` | Runtime.getRuntime().exec() 示例 | `bash -c` 执行命令；注释中含运行 Shell 脚本的写法 |
| Linux/macOS | `com.darkmi.springboot.shell.linux.ProcessBuilderDemo` | ProcessBuilder 示例 | `bash -c` 执行命令 |
| Linux/macOS | `com.darkmi.springboot.shell.linux.PingDemo` | PING 示例 | `ping -c 3 <域名>`，可用运行参数覆盖域名 |
| Linux/macOS | `com.darkmi.springboot.shell.linux.HostDemo` | HOST 示例 | `host -t a <域名>` + 正则提取 IPv4（需安装 dnsutils / bind-utils）|

## 运行

方式一：IDEA 中直接运行对应类的 `main` 方法。

方式二：Gradle 任务（见 `build.gradle.kts` 中的 `demoMainClasses`）：

```bash
# Windows 程序（在 Windows 上运行）
./gradlew :springboot-shell:runRuntimeExecWindows
./gradlew :springboot-shell:runProcessBuilderWindows
./gradlew :springboot-shell:runPingWindows

# Linux/macOS 程序（在 Linux/macOS 上运行）
./gradlew :springboot-shell:runRuntimeExecLinux
./gradlew :springboot-shell:runProcessBuilderLinux
./gradlew :springboot-shell:runPingLinux
./gradlew :springboot-shell:runHostLinux
```

PingDemo / HostDemo 可用 `-PrunArgs` 传入域名覆盖默认的 `google.com`：

```bash
./gradlew :springboot-shell:runPingWindows -PrunArgs=baidu.com
```

## 与笔记原文的差异

- `exec(String)` 单字符串版本 JDK 18 起已废弃（`@Deprecated(forRemoval = true)`），统一改为 `exec(String[] cmdarray)` 数组传参。
- 读流统一放在 `waitFor()` **之前**（笔记 HOST 示例原代码为先 `waitFor()` 再读流，存在死锁隐患）。
- Windows 程序读取子进程输出时显式指定 GBK 编码（中文 Windows 的 cmd 输出编码），避免 JDK 18+ 默认 UTF-8 解码乱码。
- 笔记中的 `hello.bat` / `hello.sh` 依赖本地文件，保留为注释；默认命令改为开箱可运行的形式。
- 修正了原文中的笔误：`List<string>` → `List<String>`、`&gt;` → `>`。
- 修复了原文 HOST 示例 IP 正则的截断 bug：`\d?` 贪婪吞位会把 `111.63.65.247` 截成 `111.63.65.24`，首尾加 `(?<!\d)` / `(?!\d)` 环视后提取完整。
- Windows 版 HostDemo 借助 WSL 执行 `host` 命令（Windows 无此命令，直接 exec 会报 `CreateProcess error=2`），需已安装 WSL 并在发行版内安装 `dnsutils`（Ubuntu/Debian）或 `bind-utils`（RHEL/CentOS）。

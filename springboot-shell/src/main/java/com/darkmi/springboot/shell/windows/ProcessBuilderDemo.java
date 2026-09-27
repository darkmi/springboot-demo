package com.darkmi.springboot.shell.windows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 《如何在 Java 中执行 Shell 命令》—— ProcessBuilder 示例（Windows 版）。
 *
 * <p>ProcessBuilder（Java 5 引入）是官方推荐替代老 Runtime.exec() 的方式：
 * 先配置（command / directory / environment / 流重定向），后 start()，同一实例可复用多次启动。</p>
 *
 * <p>ProcessBuilder 同样不解析 Shell 语法（管道 |、通配符、&amp;&amp; 等），
 * Windows 下需显式借助 cmd.exe /c 来执行命令或 bat 脚本。</p>
 */
public class ProcessBuilderDemo {

    public static void main(String[] args) {
        // 中文 Windows 的 System.out/err 默认跟随 native.encoding（GBK，-Dfile.encoding 管不到它），
        // 显式切到 UTF-8，保证 IDEA / git-bash / Gradle 日志中文不乱码
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

        ProcessBuilder processBuilder = new ProcessBuilder();

        // 运行命令：列出当前用户主目录
        processBuilder.command("cmd.exe", "/c", "dir");
        // 指定子进程工作目录（等价于 Runtime.exec 的第三个参数）
        processBuilder.directory(new File(System.getProperty("user.home")));

        // 运行 bat 文件
        // processBuilder.command("C:\\Users\\mkyong\\hello.bat");

        // 可选：把标准错误合并到标准输出，只需读取一个流
        // processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            // 必须在 waitFor() 之前读完流，否则可能死锁；
            // 中文 Windows 的 cmd 输出为 GBK 编码，需显式指定避免乱码
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(output);
            } else {
                System.err.println("命令执行失败，退出码：" + exitVal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}

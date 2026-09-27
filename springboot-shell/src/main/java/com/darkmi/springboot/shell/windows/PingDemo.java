package com.darkmi.springboot.shell.windows;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 《如何在 Java 中执行 Shell 命令》—— PING 示例（Windows 版）。
 *
 * <p>通过 ProcessBuilder 执行 cmd.exe /c ping -n 3 &lt;域名&gt;，逐行打印子进程输出。
 * Windows 的 ping 用 -n 指定次数（Linux 为 -c），可在运行参数中传入域名覆盖默认值。</p>
 */
public class PingDemo {

    public static void main(String[] args) {
        // 中文 Windows 的 System.out 默认跟随 native.encoding（GBK，-Dfile.encoding 管不到它），
        // 显式切到 UTF-8，保证 IDEA / git-bash / Gradle 日志中文不乱码
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        String domain = args.length > 0 ? args[0] : "baidu.com";

        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c", "ping -n 3 " + domain);

        try {
            Process process = processBuilder.start();

            // 中文 Windows 的 cmd 输出为 GBK 编码，需显式指定避免乱码
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}

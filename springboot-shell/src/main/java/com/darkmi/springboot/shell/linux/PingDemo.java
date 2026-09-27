package com.darkmi.springboot.shell.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 《如何在 Java 中执行 Shell 命令》—— PING 示例（Linux/macOS 版）。
 *
 * <p>通过 ProcessBuilder 执行 ping -c 3 &lt;域名&gt;，逐行打印子进程输出。
 * Linux 的 ping 用 -c 指定次数（Windows 为 -n），可在运行参数中传入域名覆盖默认值。</p>
 */
public class PingDemo {

    public static void main(String[] args) {
        String domain = args.length > 0 ? args[0] : "baidu.com";

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ping", "-c", "3", domain);

        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

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

package com.darkmi.springboot.shell.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 《如何在 Java 中执行 Shell 命令》—— ProcessBuilder 示例（Linux/macOS 版）。
 *
 * <p>ProcessBuilder（Java 5 引入）是官方推荐替代老 Runtime.exec() 的方式：
 * 先配置（command / directory / environment / 流重定向），后 start()。</p>
 *
 * <p>ProcessBuilder 本身不解析 Shell 语法（管道 |、通配符、&amp;&amp; 等），
 * 要执行这类命令需显式借助 Shell：bash -c "..."。</p>
 */
public class ProcessBuilderDemo {

    public static void main(String[] args) {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 运行 Shell 命令（借助 bash -c）
        processBuilder.command("bash", "-c", "ls -al ~");

        // 运行 Shell 脚本
        // processBuilder.command("path/to/hello.sh");

        // 可选：Java 8+ 可让子进程直接继承当前进程的控制台 IO
        // processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            // 必须在 waitFor() 之前读完流，否则可能死锁
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

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

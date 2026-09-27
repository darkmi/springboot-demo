package com.darkmi.springboot.shell.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 《如何在 Java 中执行 Shell 命令》—— Runtime.getRuntime().exec() 示例（Linux/macOS 版）。
 *
 * <p>Runtime.exec 不会经过 Shell 解释，管道 |、通配符 *、重定向 &gt;、&amp;&amp; 等语法统统无效，
 * 要执行这类命令必须显式借助 Shell：bash -c "..."。</p>
 *
 * <p>注意：exec(String) 单字符串版本 JDK 18 起已废弃，应改用 exec(String[] cmdarray) 逐个元素传参。</p>
 */
public class RuntimeExecDemo {

    public static void main(String[] args) {
        try {
            // 运行 Shell 命令（借助 bash -c）
            Process process = Runtime.getRuntime().exec(
                    new String[]{"bash", "-c", "ls -al ~"});

            // 运行 Shell 脚本
            // Process process = Runtime.getRuntime().exec(
            //         new String[]{"path/to/hello.sh"});

            StringBuilder output = new StringBuilder();

            // 必须在 waitFor() 之前读完流，否则输出塞满缓冲区（约 64KB）后会造成死锁
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

package com.darkmi.springboot.shell;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class PingLinux {

    public static void main(String[] args) {
        // 中文 Windows 的 System.out 默认跟随 native.encoding（GBK，-Dfile.encoding 管不到它），
        // 显式切到 UTF-8，保证 IDEA / git-bash / Gradle 日志中文不乱码
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        String domain = args.length > 0 ? args[0] : "baidu.com";

        // wsl.exe -e <cmd>：不经 shell 直接执行，参数逐个传递
        ProcessBuilder processBuilder = new ProcessBuilder(
                "wsl.exe", "-e", "ping", "-c", "3", domain);

        // 合并错误流到标准输出，WSL 内命令缺失等报错也能直接看到
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            // 必须在 waitFor() 之前读完流，否则可能死锁；wsl.exe 输出为 UTF-8
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
            // 报 CreateProcess error=2 通常是未安装 WSL 或 wsl.exe 不在 PATH
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}

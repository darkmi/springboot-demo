package com.darkmi.springboot.shell;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PingWin {

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

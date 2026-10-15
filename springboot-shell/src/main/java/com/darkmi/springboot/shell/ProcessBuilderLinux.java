package com.darkmi.springboot.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ProcessBuilderLinux {

    public static void main(String[] args) {
        // 中文 Windows 的 System.out/err 默认跟随 native.encoding（GBK，-Dfile.encoding 管不到它），
        // 显式切到 UTF-8，保证 IDEA / git-bash / Gradle 日志中文不乱码
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

        ProcessBuilder processBuilder = new ProcessBuilder(buildCommand("ls -al ~"));

        // 指定子进程工作目录为用户主目录（Git Bash / WSL 均会进入对应目录）
        processBuilder.directory(new File(System.getProperty("user.home")));

        // 把标准错误合并到标准输出：WSL 内命令缺失等报错也能直接看到，只需读取一个流
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            // 必须在 waitFor() 之前读完流，否则可能死锁；Git Bash / WSL 输出均为 UTF-8
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

    /**
     * 组装可执行命令：优先 Git Bash（bash -c），未安装则退回 WSL（wsl.exe -e bash -lc）。
     */
    private static String[] buildCommand(String linuxCmd) {
        File gitBash = findGitBash();
        if (gitBash != null) {
            return new String[]{gitBash.getAbsolutePath(), "-c", linuxCmd};
        }
        return new String[]{"wsl.exe", "-e", "bash", "-lc", linuxCmd};
    }

    /**
     * 查找 Git for Windows 自带的 bash.exe（常见两个安装位置）。
     */
    private static File findGitBash() {
        String localAppData = System.getenv("LOCALAPPDATA");
        String programFiles = System.getenv("ProgramFiles");
        if (localAppData != null) {
            File f = new File(localAppData, "Programs\\Git\\bin\\bash.exe");
            if (f.exists()) {
                return f;
            }
        }
        if (programFiles != null) {
            File f = new File(programFiles, "Git\\bin\\bash.exe");
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }
}

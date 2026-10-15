package com.darkmi.springboot.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class RuntimeExecLinux {

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

        // 方式一：Git Bash 模拟（~ 即 Windows 用户主目录）
        File gitBash = findGitBash();
        if (gitBash != null) {
            System.out.println(">>> 方式一：Git Bash = " + gitBash);
            run(new String[]{gitBash.getAbsolutePath(), "-c", "ls -al ~"});
        } else {
            System.out.println(">>> 方式一：未找到 Git Bash，跳过");
        }

        // 方式二：WSL 模拟（~ 是 Linux 发行版内的 /home/<user>）
        System.out.println(">>> 方式二：WSL");
        // wsl.exe 不在 PATH 或未安装发行版时抛 IOException，由 run() 捕获打印
        run(new String[]{"wsl.exe", "-e", "bash", "-lc", "ls -al ~"});
    }

    /**
     * 执行命令并打印结果。必须在 waitFor() 之前读完流，
     * 否则输出塞满缓冲区（约 64KB）后会造成死锁。
     */
    private static void run(String[] command) {
        try {
            // 第二、三个参数：环境变量用父进程默认（null），工作目录指定为用户主目录
            Process process = Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.home")));

            StringBuilder output = new StringBuilder();

            // Git Bash / WSL 输出均为 UTF-8
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
                // 进程非零退出不会抛异常，需主动检查退出码
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

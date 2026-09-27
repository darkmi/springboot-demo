package com.darkmi.springboot.shell.windows;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 《如何在 Java 中执行 Shell 命令》—— HOST 示例（Windows 版，借助 WSL）。
 *
 * <p>host 命令仅 *nix 系统可用，Windows 上直接 exec("host ...") 会报
 * CreateProcess error=2（系统找不到指定的文件）。本示例通过 wsl.exe 把命令
 * 转发给 WSL 的 Linux 发行版（如 Ubuntu）执行，等效于 linux 包下的 HostDemo。</p>
 *
 * <p>前置条件：
 * 1. 已安装 WSL 并至少有一个发行版（wsl --install；wsl -l -v 查看）；
 * 2. 发行版内已安装 host 命令：Ubuntu/Debian 执行 sudo apt install dnsutils
 *    （RHEL/CentOS 为 bind-utils）。</p>
 *
 * <p>注意：wsl.exe 的管道输出为 UTF-8，按 UTF-8 解码即可，无需像 cmd 那样用 GBK。</p>
 */
public class HostDemo {

    // 在笔记原正则基础上首尾加环视：(?<!\d) 避免从更长数字串中间截断；
    // (?!\d) 修复 \d? 贪婪吞位导致末位丢失（如 111.63.65.247 被截成 111.63.65.24）
    private static final String IPADDRESS_PATTERN =
            "(?<!\\d)([01]?\\d\\d?|2[0-4]\\d|25[0-5])"
                    + "\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"
                    + "\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"
                    + "\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])(?!\\d)";

    private static final Pattern IP_PATTERN = Pattern.compile(IPADDRESS_PATTERN);

    public static void main(String[] args) {
        // 中文 Windows 的 System.out 默认跟随 native.encoding（GBK，-Dfile.encoding 管不到它），
        // 显式切到 UTF-8，保证 IDEA / git-bash / Gradle 日志中文不乱码
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        String domainName = args.length > 0 ? args[0] : "baidu.com";

        // wsl -e <cmd>：不经 shell 直接执行，参数逐个传递
        // 合并错误流到标准输出，WSL 内命令缺失等报错也能直接看到
        ProcessBuilder processBuilder = new ProcessBuilder(
                "wsl.exe", "-e", "host", "-t", "a", domainName);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            // 必须在 waitFor() 之前读完流，否则可能死锁；wsl.exe 输出为 UTF-8
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();

            int exitVal = process.waitFor();
            System.out.println(output);

            List<String> list = getIpAddress(output.toString());

            if (!list.isEmpty()) {
                System.out.printf("%s has address : %n", domainName);
                for (String ip : list) {
                    System.out.println(ip);
                }
            } else {
                System.out.printf("%s has NO address. %n", domainName);
            }

            System.out.println("\nExited with error code : " + exitVal);
        } catch (IOException e) {
            // 报 CreateProcess error=2 通常是未安装 WSL 或 wsl.exe 不在 PATH
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private static List<String> getIpAddress(String msg) {
        List<String> ipList = new ArrayList<>();

        if (msg == null || msg.isEmpty()) {
            return ipList;
        }

        Matcher matcher = IP_PATTERN.matcher(msg);
        while (matcher.find()) {
            ipList.add(matcher.group(0));
        }

        return ipList;
    }
}

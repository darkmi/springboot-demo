package com.darkmi.springboot.shell.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 《如何在 Java 中执行 Shell 命令》—— HOST 示例（仅 *nix 系统可用）。
 *
 * <p>执行 Shell 命令 host -t a &lt;域名&gt; 获取所有关联到该域名的 IP 地址，
 * 之后使用正则表达式提取所有 IPv4 地址并显示出来。</p>
 *
 * <p>host 命令由 BIND 提供，Debian/Ubuntu 需安装 dnsutils，RHEL/CentOS 需安装 bind-utils。</p>
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
        String domainName = args.length > 0 ? args[0] : "baidu.com";
        String command = "host -t a " + domainName;

        String output = executeCommand(command);

        List<String> list = getIpAddress(output);

        if (!list.isEmpty()) {
            System.out.printf("%s has address : %n", domainName);
            for (String ip : list) {
                System.out.println(ip);
            }
        } else {
            System.out.printf("%s has NO address. %n", domainName);
        }
    }

    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();

        try {
            // exec(String) 单字符串版本 JDK 18+ 已废弃，按空白拆分成数组传入
            Process p = Runtime.getRuntime().exec(command.split("\\s+"));

            // 必须先读完流再 waitFor()：顺序颠倒时，输出塞满缓冲区（约 64KB）
            // 后父子进程会互相等待造成死锁
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        return output.toString();
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

package com.darkmi.springboot.shell.windows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 《如何在 Java 中执行 Shell 命令》—— Runtime.getRuntime().exec() 示例（Windows 版）。
 *
 * <p>Runtime.exec 只是把程序交给操作系统启动，中间不经过任何 Shell 解释，
 * 管道 |、通配符 *、重定向 &gt;、&amp;&amp; 等语法统统无效，
 * 因此 Windows 下必须显式借助 cmd /c 来执行命令或 bat 脚本。</p>
 *
 * <p>注意：exec(String) 单字符串版本会按空白拆分参数，路径含空格时会解析出错，
 * JDK 18 起已被标记为 @Deprecated(forRemoval = true)，应改用 exec(String[] cmdarray) 逐个元素传参。</p>
 */
public class RuntimeExecDemo {

    public static void main(String[] args) {
        // 中文 Windows 的 System.out/err 默认跟随 native.encoding（GBK，-Dfile.encoding 管不到它），
        // 显式切到 UTF-8，保证 IDEA / git-bash / Gradle 日志中文不乱码
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

        try {
            // 运行命令：列出当前用户主目录（第三个参数指定子进程工作目录）
            Process process = Runtime.getRuntime().exec(
                    new String[]{"cmd", "/c", "dir"},
                    null,
                    new File(System.getProperty("user.home")));

            // 运行 bat 文件（把目录换成 hello.bat 所在目录即可）
            // Process process = Runtime.getRuntime().exec(
            //         new String[]{"cmd", "/c", "hello.bat"},
            //         null,
            //         new File("C:\\Users\\mkyong\\"));

            StringBuilder output = new StringBuilder();

            // 必须在 waitFor() 之前读完流：两个独立的流若只读其一，
            // 另一个的缓冲区写满（约 64KB）后子进程会被卡住造成死锁
            // 中文 Windows 的 cmd 输出为 GBK 编码，需显式指定；
            // JDK 18+ 默认按 UTF-8（JEP 400）解码，直接读会乱码
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));

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
}

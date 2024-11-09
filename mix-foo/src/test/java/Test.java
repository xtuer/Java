import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Test {
    public static void main(String[] args) throws IOException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            // 获取 MAC 地址
            byte[] mac = networkInterface.getHardwareAddress();

            // 如果 MAC 地址不为 null，则输出网卡信息
            if (mac != null) {
                System.out.println("Interface Name: " + networkInterface.getName());
                System.out.println("Display Name: " + networkInterface.getDisplayName());
                System.out.print("MAC Address: ");

                // 将 MAC 地址转换为常见的十六进制格式
                for (int i = 0; i < mac.length; i++) {
                    System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
                }
                System.out.println("\n-----------");
            }
        }
    }

    public static String toAddress(byte[] hardwareAddress) {
        if (hardwareAddress != null) {
            String[] hexadecimalFormat = new String[hardwareAddress.length];

            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimalFormat[i] = String.format("%02X", hardwareAddress[i]);
            }

            return String.join(":", hexadecimalFormat);
        }

        return null;
    }
}

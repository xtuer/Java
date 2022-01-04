package serialize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 自定义对象的序列化和反序列化
 */
public class Main {
    public static void main(String[] args) {
        // [1] 创建对象
        Customer customer = new Customer();
        customer.setId(10);
        customer.setName("Alice");

        // [2] 对象序列化字节数组: 用于保存到文件、网络传输
        byte[] payload = serialize(customer);
        System.out.println(Arrays.toString(payload));

        // [3] 字节数组反序列化为对象
        Customer temp = deserialize(payload);
        System.out.println(temp);

        // 大小端: 先存储高位字节 (大端) 还是先存储低位字节 (小端)
        // 大端模式: 即高位字节放在内存的低地址端，低位字节放在内存的高地址端
        // 小端模式: 即低位字节放在内存的低地址端，高位字节放在内存的高地址端
        // ARM Mac, Intel Linux 系统使用 LITTLE_ENDIAN
        System.out.println("System: " + ByteOrder.nativeOrder());
        // ByteBuffer 默认使用 BIG_ENDIAN
        System.out.println(ByteBuffer.allocate(1).order());
    }

    /**
     * 序列化客户为字节数组
     *
     * @param customer 客户对象
     * @return 返回序列化得到的字节数组
     */
    public static byte[] serialize(Customer customer) {
        byte[] nameBytes;

        if (customer.getName() != null) {
            nameBytes = customer.getName().getBytes(StandardCharsets.UTF_8);
        } else {
            nameBytes = new byte[0];
        }

        // 结构: ID+字符串长度+字符串的字节内容
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + nameBytes.length);
        buffer.putInt(customer.getId());
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);

        return buffer.array();
    }

    /**
     * 把字节数组反序列化为客户对象
     *
     * @param payload 客户序列化得到的字节数组
     * @return 返回反序列化得到的客户对象，序列化失败返回 null
     */
    public static Customer deserialize(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            int id = buffer.getInt();
            int nameBytesLength = buffer.getInt();
            byte[] nameBytes = new byte[nameBytesLength];
            buffer.get(nameBytes, 0, nameBytesLength);
            String name = new String(nameBytes, StandardCharsets.UTF_8);

            Customer customer = new Customer();
            customer.setId(id);
            customer.setName(name);

            return customer;
        } catch (Exception ex) {
            // 当 payload 不完整时抛出异常 (读取时字节不够)
            ex.printStackTrace();
        }

        return null;
    }
}

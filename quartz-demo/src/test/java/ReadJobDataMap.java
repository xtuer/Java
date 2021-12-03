import com.example.demo.bean.Foo;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;

import java.io.*;
import java.sql.*;

/**
 * 读取 Quartz 的数据
 */
public class ReadJobDataMap {
    @Test
    public void read() throws Exception {
        String url  = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true";
        String user = "root";
        String pass = "root";

        // String url  = "jdbc:mysql://192.168.1.118:3306/ndtmdb?&useSSL=false";
        // String user = "sys_admin";
        // String pass = "manager";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "SELECT DESCRIPTION, JOB_DATA FROM qrtz_job_details";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getString(1));

                // byte[] data = rs.getBytes(2);
                // ByteArrayInputStream bis = new ByteArrayInputStream();
                // ObjectInput in = new ObjectInputStream(bis);

                Blob blob = rs.getBlob(2);
                ObjectInput in = new ObjectInputStream(blob.getBinaryStream());
                JobDataMap map = (JobDataMap) in.readObject();

                for (Object obj : map.values()) {
                    System.out.println(obj);
                }
            }
        }
    }

    @Test
    public void serialize() throws Exception {
        Foo foo = new Foo();
        foo.setName("Alice");

        try (ObjectOutputStream out =  new ObjectOutputStream(new FileOutputStream("D:/foo.data"))) {
            out.writeObject(foo);
        }
    }

    @Test
    public void deserialize() throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("D:/foo.data"))) {
            Foo foo = (Foo) in.readObject();
            System.out.println(foo.getName());
            System.out.println(foo.getAge());
        }
    }
}

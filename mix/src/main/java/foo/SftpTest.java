package foo;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SftpTest {
    public static void main (String[] args){
        String serverport = "22";
        String serverhost = "192.168.1.225";
        String username = "ndtdev";
        String password = "ndtdev123";
        int timeout = 5000; // 单位: 毫秒
        try{
            int port = 22;
            String sessionkey = null;
            if(serverport != null && !serverport.trim().equals(""))
                port = Integer.parseInt(serverport);
            JSch jsch=new JSch();

            Session session=jsch.getSession(username, serverhost, port);
            jsch.setKnownHosts(System.getProperty("user.home")+"/.ssh/known_hosts");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);

            System.out.println("Password has been set");
            if(timeout > 0){
                session.setTimeout(timeout);
                System.out.println("Timeout has been set");
            }
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            System.out.println("Added PreferredAuthentications");
            session.connect();
            System.out.println("Connected");

            Channel channel = session.openChannel("sftp");
            System.out.println("Channel opened");
            channel.connect();
            System.out.println("Connected");
            if (channel.isConnected()){
                sessionkey = serverhost  + ":" + serverport + ":" + username + ":" + Thread.currentThread().hashCode();
            }
            System.out.println("Session: "+sessionkey);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

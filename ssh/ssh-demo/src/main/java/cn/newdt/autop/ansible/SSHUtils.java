package cn.newdt.autop.ansible;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;


/***
 * ansible的操作
 * @author duanty
 *
 */
@Component
public class SSHUtils {
    /***
     * SSH登陆Ansible Server
     * @return Connection 链接
     */
    public Connection SSHConnect(){

        try {
            // 创建链接
            Connection conn = new Connection("192.168.12.101", 22);
            // System.out.println("set up connections");
            conn.connect();
            // 添加信任
            boolean isAuthenticated = conn.authenticateWithPassword("root", "Newdt@cn");
            if (isAuthenticated == false) {
                // System.out.println("--------");
                throw new IOException("Authorication failed");
            }
            return conn;
        }catch (Exception e) {
            throw new RuntimeException("Can not access the remote machine");
            //String error = "can not access the remote machine";
        }
    }

    /***
     * 执行命令
     * @param conn 登陆后返回的Connection链接
     * @param command 执行的命令
     * @param isLocal true表示执行本地命令行，false表示执行远程ssh命令
     * @param commandArray 如果是本地命令行，执行ansible的时候需要使用数组形式
     * @return JSONArray 执行结果
     * @throws IOException
     */
    public String runCommand(Connection conn,String command, boolean isLocal, String[] commandArray) throws IOException{
        String result="";
        //当isLocal为True时，说明Ansible服务器和程序在同一机器上，调用本地命令行
        if(isLocal){
            BufferedInputStream bisInputStream = null;
            BufferedReader brInputStream = null;
            try {
                Process process;
                if(StringUtils.isEmpty(commandArray)){
                    process = Runtime.getRuntime().exec(command);
                }else{
                    process = Runtime.getRuntime().exec(commandArray);
                }
                // 开启一个单独的线程处理errorStream
                new Thread(new Runnable() {
                    public void run() {
                        BufferedInputStream bisErrorStream = new BufferedInputStream(process.getErrorStream());
                        BufferedReader brErrorStream = new BufferedReader(new InputStreamReader(bisErrorStream));
                        try {
                            while ((brErrorStream.readLine()) != null) {
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e.toString());
                        } finally {
                            try {
                                bisErrorStream.close();
                                brErrorStream.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e.toString());
                            }
                        }
                    }
                }).start();

                bisInputStream = new BufferedInputStream(
                        process.getInputStream());
                brInputStream = new BufferedReader(new InputStreamReader(bisInputStream));
                String line;
                while ((line = brInputStream.readLine()) != null) {
                    result += line + "\n";
                }
                process.waitFor();

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e.toString());
            } finally {
                if(bisInputStream != null){
                    bisInputStream.close();
                }
                if(brInputStream != null){
                    brInputStream.close();
                }
            }
        }else{
            InputStream stdout = null;
            BufferedReader br = null;
            Session sess = null;
            // 执行命令
            try {
                sess = conn.openSession();
                sess.execCommand(command);
                // System.out.println("The execute command output is:");
                // 返回结果
                stdout = new StreamGobbler(sess.getStdout());
                br = new BufferedReader(new InputStreamReader(stdout));
                while (true) {
                    String line = br.readLine();
                    if (line == null){
                        break;
                    }
                    result += line + "\n";
                }
                // System.out.println("Exit code "+sess.getExitStatus());

            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            } finally {
                if(sess != null){
                    sess.close();
                }
                if(br != null){
                    br.close();
                }
                if(stdout != null){
                    stdout.close();
                }
            }
        }

        return result;
    }

    /**
     * 获取自定义连接
     * @param ip
     * @param userName
     * @param password
     * @param cmd
     * @return
     */
    public String CustomSSHCmd(String ip, String userName, String password, String cmd) throws Exception {
        boolean flag = false;
        Connection conn = null;
        String result = "";
        try {
            conn = new Connection(ip);
            conn.connect();//连接
            flag = conn.authenticateWithPassword(userName, password);//认证
            if (flag) {
            }
            if (conn != null) {
                Session session = conn.openSession();//打开一个会话
                session.execCommand(cmd);//执行命令

                result = processStdout(session.getStdout(), "UTF-8");
                //如果为得到标准输出为空，说明脚本执行出错了
                if (StringUtils.isEmpty(result)) {
                    result = processStdout(session.getStderr(), "UTF-8");
                } else {
                }
                conn.close();
                session.close();
                return result;
            }
        } catch (IOException e) {
            throw e;
        }
        return null;
    }

    private String processStdout(InputStream in, String charset){
        InputStream  stdout = new StreamGobbler(in);
        StringBuffer buffer = new StringBuffer();;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout,charset));
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line + "\n");
            }
        } catch (UnsupportedEncodingException e) {
        } catch (IOException e) {
        }
        return buffer.toString();
    }

}

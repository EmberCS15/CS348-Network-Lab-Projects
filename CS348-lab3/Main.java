import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main{
    public static AtomicInteger curr_request = new AtomicInteger();
    public static void main(String args[]) throws Exception {
        ServerSocket server = new ServerSocket(1234);
        System.out.println("Listening for connection on port 1234 ....");
        File file = new File("config.properties");
        FileInputStream fileInput = new FileInputStream(file);
        Properties prop = new Properties();
        prop.load(fileInput);
        fileInput.close();
        int max_request = Integer.parseInt(prop.getProperty("max_request"));
        curr_request.set(0);
        String default_file = prop.getProperty("default_html");
        HashSet<String> blockedIP = new HashSet<String>();
        String s[]=prop.getProperty("ip_block").split(",");
        for(String str:s){
            blockedIP.add(str);
        }
        while (true) {
            Socket socket = server.accept();
            String con = socket.getRemoteSocketAddress().toString();
            ServerThread serverThread = null;
            if(!blockedIP.contains(con.substring(1,con.indexOf(":"))) && curr_request.get()<max_request){
                serverThread = new ServerThread("Thread"+curr_request,default_file,socket,1);
                curr_request.getAndIncrement();
            }else if(blockedIP.contains(con.substring(1,con.indexOf(":")))){
                System.out.println("Access Denied");
                serverThread = new ServerThread("Thread"+curr_request,"page400.html",socket,3);
                curr_request.getAndIncrement();
            }else if(curr_request.get() >= max_request){
                System.out.println("Bad Request");
                serverThread = new ServerThread("Thread"+curr_request,"page400.html",socket,2);
                curr_request.getAndIncrement();
            }
        }
    }
}
import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {
    Thread t;
    String default_file;
    Socket socket;
    int mode;
    public ServerThread(String name,String default_file,Socket socket,int mode){
        this.default_file=default_file;
        t = new Thread(this,name);
        this.socket=socket;
        this.mode = mode;
        t.start();
    }
    @Override
    public void run(){
        String result="";
        try{
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            String header_first = inFromClient.readLine();
            System.out.println("Client Connection Recieved : "+header_first);
            if(header_first == null || header_first.isEmpty()){
                inFromClient.close();
                return;
            }
            String fileName = header_first.substring(header_first.indexOf("/")+1);
            String httpVersion = header_first.substring(header_first.lastIndexOf(" ")+1);
            fileName = fileName.substring(0,fileName.indexOf(" "));
            fileName=(fileName.isEmpty()||mode==0||mode==2||mode==3)?default_file:fileName;
            try{
                ReadFile f = new ReadFile();
                result = f.read(fileName);
                if(mode == 1)
                    result = httpVersion+" 200 OK\r\n\r\n"+result;
                else if(mode == 2) result = httpVersion+" 400 Bad Request\r\n\r\n";
                else if(mode == 3) result = httpVersion+" 403 Access Denied\r\n\r\n";
            }catch(Exception exc){
                System.out.println("Error Reading output File");
                ReadFile ferr = new ReadFile();
                result = ferr.read("page404.html");
                result=httpVersion+" 404 Not Found\r\n\r\n";
            }finally{
                socket.getOutputStream().write(result.getBytes("UTF-8"));
                if(inFromClient!=null)
                    inFromClient.close();
                if(socket!=null){
                    socket.close();
                }
                Thread.sleep(5000);
                Main.curr_request.getAndDecrement();
            }
        }catch(Exception exc){
            exc.printStackTrace();
            System.out.println("Error in main Thread Code");
        }
    }
}

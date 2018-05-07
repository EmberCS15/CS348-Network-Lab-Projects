import java.net.*;
import java.io.*;
public class SingleTCPServer {
    public static void main(String args[]) throws Exception{
        ServerSocket server = new ServerSocket(1234);
        System.out.println("Listening for connection on port 1234 ....");
        while (true) {
            try (Socket socket = server.accept()) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String header_first = inFromClient.readLine();
                if(header_first == null || header_first.isEmpty()){
                    inFromClient.close();
                    continue;
                }
                System.out.println("Client Connection Recieved : "+header_first);
                String fileName = header_first.substring(header_first.indexOf("/")+1),result="";
                String httpVersion = header_first.substring(header_first.lastIndexOf(" ")+1);
                fileName = fileName.substring(0,fileName.indexOf(" "));
                try{
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
                    String fileLine = fileReader.readLine();
                    while(fileLine!=null){
                        result+=fileLine+"\n";
                        fileLine = fileReader.readLine();
                    }
                    fileReader.close();
                    result = httpVersion+" 200 OK\r\n\r\n"+result;
                    //result = httpVersion+" 200 OK\r\n\r\n";
                }catch(FileNotFoundException e){
                    System.out.println("File not Present : Maybe Favicon");
                    //e.printStackTrace();
                    BufferedReader errorReader = new BufferedReader(new FileReader("page404.html"));
                    String errorLine = errorReader.readLine();
                    while(errorLine!=null){
                        result+=errorLine+"\n";
                        errorLine = errorReader.readLine();
                    }
                    errorReader.close();
                    //result=httpVersion+" 404 Not Found\r\n\r\n"+result;
                    result=httpVersion+" 404 Not Found\r\n\r\n";
                    //result="HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\n\r\n";
                }finally{
                    //System.out.println("Result : "+result);
                    socket.getOutputStream().write(result.getBytes("UTF-8"));
                    if(inFromClient!=null)
                        inFromClient.close();
                    if(socket!=null)
                        socket.close();
                }
            }
        }
    }
}

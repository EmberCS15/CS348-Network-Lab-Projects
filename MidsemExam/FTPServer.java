import java.io.*;
import java.util.*;
import java.net.*;
public class FTPServer{
    static String readFile(String path) throws Exception{
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String s=br.readLine(),result="";
        while(s!=null){
            if(!s.isEmpty()){
                result+=s+"\n";
            }
            s=br.readLine();
        }
        result+="\n";
        br.close();
        fr.close();
        return result;
    }
    static void writeFile(String content,String fileName) throws Exception{
        File f = new File(fileName);
        f.createNewFile();
        FileWriter fw = new FileWriter(fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        System.out.println("Read File Success");
        bw.close();
        fw.close();
    }
    static String listAllFiles(String path){
        File f = new File(path);
        String s[]=f.list();
        String r="";
        for(String files:s){
            r+=files+"\n";
        }
        r+="\n";
        return r;
    }
    public static void main(String args[]) throws Exception{
        Scanner sc = new Scanner(System.in);
        String result="",s="";
        ServerSocket connServer = new ServerSocket(6543);
        ServerSocket dataServer = new ServerSocket(3452);
        while(true){
            Socket connClient = connServer.accept();
            Socket dataClient = dataServer.accept();
            BufferedReader connReader = new BufferedReader(new InputStreamReader(connClient.getInputStream()));
            BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataClient.getInputStream()));
            DataOutputStream connClientWriter = new DataOutputStream(connClient.getOutputStream());
            DataOutputStream dataClientWriter = new DataOutputStream(dataClient.getOutputStream());
            String cmd = "";
            String serverDir = "./ServerDirectory";
            String d="./ServerDirectory/";
            boolean is = true;
            File dir=null;
            while(is){
                cmd = connReader.readLine();
                //System.out.println("Recieved : "+cmd);
                String c = "",val="";
                if(cmd.indexOf(" ")!=-1){
                    c = cmd.substring(0,cmd.indexOf(" "));
                    val = cmd.substring(cmd.indexOf(" ")+1);
                }else c=cmd;
                System.out.println("Recieved:"+cmd+" C:"+c.toLowerCase());
                switch(c.toLowerCase()){
                    case "rwd":
                            String temp = d;
                            result="";
                            if(val.lastIndexOf("/")==-1)
                                temp+=val+"/";
                            else temp+=val;
                            System.out.println("d = "+temp);
                            dir = new File(temp);
                            System.out.println(temp+" "+dir.isDirectory());
                            if(dir.isDirectory()){
                                d=temp;
                                result = listAllFiles(d);
                                System.out.println("Result CWD = "+result);
                                connClientWriter.writeBytes(result);
                            }else{
                                connClientWriter.writeBytes("-100 File Not Found\n\n");
                            }
                        break;
                    case "ls":result="";
                            result=listAllFiles(d);
                            connClientWriter.writeBytes(result);
                            break;
                    case "send":result="";
                            s=dataReader.readLine();
                            //System.out.println("File Recieved : "+s);
                            while(s!=null && !s.isEmpty()){
                                result+=s+"\n";
                                s=dataReader.readLine();
                            }
                            //System.out.println("File Content : "+result);
                            writeFile(result,d+val);
                            connClientWriter.writeBytes("200 File Written Successfully\n\n");
                            break;
                    case "store":result="";
                            val = d+val;
                            dir = new File(val);
                            System.out.println(val);
                            if(dir.exists()){
                                result = readFile(val);
                                connClientWriter.writeBytes("202 File Sent Successfully\n\n");
                                dataClientWriter.writeBytes(result);
                            }else{
                                connClientWriter.writeBytes("-100 File Not Found\n\n");
                            }
                            break;
                    case "quit":is = false;
                    default:
                        System.out.println("In default");
                        connClientWriter.writeBytes("103 Invalid Command\n\n");
                }
            }
            connClient.close();
            dataClient.close();
        }
    }
}

import java.io.*;
import java.util.*;
import java.net.*;
public class FTPClient {
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
        Socket connSocket = new Socket("172.16.27.162",6543);
        Socket dataSocket = new Socket("172.16.27.162",3452);
        BufferedReader connReader = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
        BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        DataOutputStream connServerWriter = new DataOutputStream(connSocket.getOutputStream());
        DataOutputStream dataServerWriter = new DataOutputStream(dataSocket.getOutputStream());
        String cmd="";
        boolean is = true;
        File dir  = null;
        String cdir = "./ClientDirectory/";
        String clientDirectory = "./ClientDirectory/";
        while(is){
            System.out.print(">>");
            cmd = sc.nextLine();
            String c="",val="",returnCode="";
            if(cmd.indexOf(" ")!=-1){
                c = cmd.substring(0,cmd.indexOf(" "));
                val = cmd.substring(cmd.indexOf(" ")+1);
            }else{
                c = cmd;
            }
            //System.out.println(cmd+" "+c+" "+val);
            cmd+="\n";
            switch(c.toLowerCase()){
                case "cwd":
                    String temp = cdir;
                    temp+=val;
                    if(val.lastIndexOf("/")==-1)
                        temp+="/";
                    dir = new File(temp);
                    if(dir.isDirectory()){
                        cdir=temp;
                        result = listAllFiles(cdir);
                    }else System.out.println("Enter a valid directory");
                    break;
                case "rwd":connServerWriter.writeBytes(cmd);
                    returnCode = connReader.readLine();
                    result="";
                    while(returnCode!=null && !returnCode.isEmpty()){
                        result+=returnCode+"\n";
                        returnCode = connReader.readLine();
                    }
                    System.out.println(result);
                    break;
                case "ls":connServerWriter.writeBytes(cmd);
                    returnCode = connReader.readLine();
                    result="";
                    while(returnCode!=null && !returnCode.isEmpty()){
                        result+=returnCode+"\n";
                        returnCode = connReader.readLine();
                    }
                    System.out.println(result);
                    break;
                case "send":
                    result="";
                    val=clientDirectory+val;
                    dir = new File(val);
                    if(dir.exists()){
                        connServerWriter.writeBytes(cmd);
                        String fileContent = readFile(val);
                        //System.out.println("File read : "+fileContent);
                        dataServerWriter.writeBytes(fileContent);
                        returnCode = connReader.readLine();
                        while(returnCode!=null && !returnCode.isEmpty()){
                            result+=returnCode+"\n";
                            returnCode = connReader.readLine();
                        }
                        System.out.println(result);
                    }else System.out.println("Please enter a valid file name");
                    break;
                case "store":connServerWriter.writeBytes(cmd);
                    result="";
                    val = cdir+val;
                    String m = connReader.readLine();
                    returnCode = m;
                    while(returnCode!=null && !returnCode.isEmpty()){
                        returnCode = connReader.readLine();
                    }
                    System.out.println(m);
                    if(!m.contains("-100 File Not Found")){
                        s=dataReader.readLine();
                        while(s!=null && !s.isEmpty()){
                            result = s+"\n";
                            s=dataReader.readLine();
                        }
                        writeFile(result,val);
                    }
                    break;
                case "quit":is = false;
                    break;
                case "cls":result="";
                    result=listAllFiles(cdir);
                    System.out.println(result);
                    break;
                default:System.out.println("Please enter a valid command");
            }
        }
        connSocket.close();
        dataSocket.close();
    }
}

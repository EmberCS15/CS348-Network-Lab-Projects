import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChordUtil {
    public static String getShaValue(String localAddress) throws Exception{
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(localAddress.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        //Returns a 40 length SHA-1 String
        return sb.toString();
    }

    /*I have no idea how to do this .God Almighty Help me.
    May the sun keep shining on the other side of the moon.GodSpeed.
    Please Improve Function Leads to Duplicate Values.*/
    public static int mapShaToRange(String shaId){
        String binary = new BigInteger(shaId.getBytes()).toString(2);
        //System.out.println("As binary: "+binary);
        int r = 0,iter=0;
        for(int i=31;i<160;i+=32){
            r = r+((binary.charAt(i)-'0')<<iter);
            iter++;
        }
        //System.out.println("Sha Mappped to : "+r);
        return r;
    }

    public static String getStringFromStream(InputStream istream) throws Exception{
        BufferedReader is = new BufferedReader(new InputStreamReader(istream));
        String s = is.readLine();
        String result="";
        while(s!=null && !s.isEmpty()){
            //System.out.println("Received Request : "+s+"\n");
            result += s;
            s=is.readLine();
        }
        //System.out.println("Exited the system");
        return result;
    }

    public static String executeRemoteFunction(InetSocketAddress broadcastNode,String request) {
        try {
            Socket clientSocket = new Socket(broadcastNode.getAddress(), broadcastNode.getPort());
            DataOutputStream outToPeer = new DataOutputStream(clientSocket.getOutputStream());
            outToPeer.writeBytes(request+"\n\n");
            Thread.sleep(80);
            String send = getStringFromStream(clientSocket.getInputStream());
            return send;
        }catch (Exception exc){
            System.out.println("Execute Function failed for Request : "+request+" Location = "+broadcastNode.toString());
            if(request.startsWith("UALIVE")){
                return "NO";
            }
            //exc.printStackTrace();
        }
        return null;
    }

    //base is predecessor type
    public static int calculateRelativeId(int id,int base,int range){
        int rem = id-base;
        if(rem<=0){
            rem+=(1<<range);
        }
        return rem;
    }

    public static InetSocketAddress getInetSocketAddresss(String ip){
        Pattern p = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):(([0-9]){1,4}|[0-6][0-5][0-5][0-3][0-5])");
        Matcher m = p.matcher(ip);
        boolean b = m.matches();
        if(b){
            InetAddress m_ip;
            String t_ip = ip.substring(0,ip.lastIndexOf(":"));
            int port = Integer.parseInt(ip.substring(ip.lastIndexOf(":")+1));
            try {
                m_ip = InetAddress.getByName(t_ip);
            }catch(Exception exc){
                System.out.println("Cannot create socket address.");
                return null;
            }
            return new InetSocketAddress(m_ip,port);
        }else{
            System.out.println("Please enter a valid IP.");
            return null;
        }
    }

    public static void createFile(String fileName,String path){
        File dir = new File(path);
        // attempt to create the directory here
        if(!dir.isDirectory()){
            dir.mkdir();
        }
        try{
            fileName = path+"/"+fileName;
            File f = new File(fileName);
            if(!f.exists())
                f.createNewFile();
        }catch (Exception exc){
            System.out.println("File Creation Failed : "+fileName);
            exc.printStackTrace();
        }
    }

    public static void writeFile(String path,String fileName,String content){
        try{
            FileWriter fw = new FileWriter(path+"/"+fileName);
            BufferedWriter bw  = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }

    public static String readFile(String fileName,String path){
        String r="";
        try{
            File f = new File(path+"/"+fileName);
            FileReader fr = new FileReader(path+"/"+fileName);
            BufferedReader br  = new BufferedReader(fr);
            String line=br.readLine();
            while(line!=null){
                r+=line;
                line = br.readLine();
            }
            br.close();
            fr.close();
            if(f.exists())
                f.delete();
            return r;
        }catch(Exception exc){
            exc.printStackTrace();
        }
        return r;
    }
}

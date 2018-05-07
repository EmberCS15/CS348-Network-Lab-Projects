import java.net.Socket;
import java.util.ArrayList;

public class RemoteProcedure implements Runnable{
    private Node local;
    private Socket talkSocket;
    Thread t;
    public RemoteProcedure(Node local,Socket socket){
        this.local = local;
        this.talkSocket = socket;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try{
            String request = ChordUtil.getStringFromStream(talkSocket.getInputStream());
            //System.out.println("Request : " + request);
            String addr="DEFAULT";
            if(request.startsWith("FIND")){
                int id = Integer.parseInt(request.substring(request.indexOf("_")+1));
                //System.out.println("ID = "+id);
                addr = local.findPredecesor(id).toString();
            }else if(request.startsWith("GETPREDECESSOR")){
                addr = local.getPredecessor().toString();
            }else if(request.startsWith("SETPRED")){
                request = request.substring(request.indexOf("_")+1);
                Data pred = new Data(request);
                local.setPredecessor(pred);
            }else if(request.startsWith("UPDATE")){
                int i=Integer.parseInt(request.substring(request.lastIndexOf("_")+1));
                String s = request.substring(request.indexOf("_")+1,request.lastIndexOf("_"));
                Data p = new Data(s);
                //System.out.println("Data Value Update : "+p.toString());
                local.updateFingerTable(p,i);
            }else if(request.startsWith("IAMPRE")){
                request = request.substring(request.indexOf("_")+1);
                Data pred = new Data(request);
                local.notified(pred);
            }else if(request.startsWith("GETSUC")){
                Data suc = local.getSuccessor();
                addr = suc.toString();
            } else if (request.startsWith("SUCFIND")){
                int id = Integer.parseInt(request.substring(request.indexOf("_")+1));
                //System.out.println("ID = "+id);
                addr = local.findSuccessor(id).toString();
            } else if(request.startsWith("READ")){
                String fileName = request.substring(request.indexOf("_")+1);
                addr = ChordUtil.readFile(fileName,local.getPath());
            }else if(request.startsWith("UALIVE")){
                //Just Accept The Message
                addr  = "YES";
            }else if(request.startsWith("TRANSFER")){
                //System.out.println("Transferring............");
                request = request.substring(request.indexOf("_")+1);
                int lower = Integer.parseInt(request.substring(0,request.indexOf("_")));
                request = request.substring(request.indexOf("_")+1);
                int upper = Integer.parseInt(request);
                addr=local.sendFiles(lower,upper);
            }else if(request.startsWith("SETFINGER")){
                request = request.substring(request.indexOf("_")+1);
                int index = Integer.parseInt(request.substring(0,request.indexOf("_")));
                request = request.substring(request.indexOf("_")+1);
                Data d = new Data(request);
                local.updateIthFinger(d,1);
            }else if(request.startsWith("INSERTKEYS")){
                request = request.substring(request.indexOf("_")+1);
                String dr = request.substring(0,request.indexOf("&"));
                Data p = new Data(dr);
                request = request.substring(request.indexOf("&")+1);
                String fileArray[]=request.split("%");
                local.initFiles(fileArray,p);
            }
            //System.out.println("The String returned  = "+addr);
            addr+="\n\n";
            talkSocket.getOutputStream().write(addr.getBytes("UTF-8"));
        }catch(Exception exc){
            System.out.println("String Creation Failed in RPC");
            exc.printStackTrace();
        }
    }
}

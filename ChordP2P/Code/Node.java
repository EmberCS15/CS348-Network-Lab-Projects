import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Node {
    //Declare Variables
    private int localId;
    private String shaId;
    private InetSocketAddress localAddress;
    private Data predecessor;
    private HashMap<Integer,Data> finger;
    private int range;
    private Data currNode;
    private String path;
    private PeerServer server;
    private FixFinger fix_finger;
    private HashMap<Integer,ArrayList<String>> files;

    public Node(InetSocketAddress localAddress,int range) throws  Exception{
        this.localAddress = localAddress;
        this.range=range;
        this.shaId = ChordUtil.getShaValue(localAddress.toString());
        this.localId = ChordUtil.mapShaToRange(shaId);
        this.finger  = new HashMap<Integer,Data>();
        this.predecessor = null;
        this.currNode = new Data(localId,this.localAddress);
        this.files = new HashMap<Integer,ArrayList<String>>();
        this.path = "./NodeFiles"+localId;
    }

    //Getter and Setters
    public int getLocalId() {
        return localId;
    }
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }
    public Data getPredecessor() {
        return predecessor;
    }
    public void setPredecessor(Data predecessor) {
        this.predecessor = predecessor;
    }
    public HashMap<Integer, Data> getFinger() {
        return finger;
    }
    public int getRange(){
        return this.range;
    }
    public Data getCurrNode() {
        return currNode;
    }
    public String getPath(){ return this.path; }
    //-------------------------------Method------------------------------

    public void join(InetSocketAddress contact){
        System.out.println("Contact : "+contact+" Local Ip : "+localAddress+" "+contact.equals(localAddress));
        if(contact!=null && !contact.equals(localAddress)){
            System.out.println("Chord Ring already Formed.........");
            initFingerTable(contact);
            System.out.println("Updating Other Entries.......");
            updateOthers();
            //KEY TRANSFER
            System.out.println("Transferring Keys............");
            transferKeys();
        }else if(contact!=null && contact.equals(localAddress)){
            //Let this be the queue that chord ring is to be made
            System.out.println("Forming chord ring..........");
            for(int i=1;i<=range;i++){
                finger.put(i,new Data(localId,localAddress));
            }
            this.setPredecessor(new Data(localId,localAddress));
            for(int i=1;i<=100;i++){
                String fileName = i+".txt";
                String fileSha="";
                try {
                    fileSha = ChordUtil.getShaValue(fileName);
                }catch(Exception exc){
                    System.out.println("SHA FILE NOT FOUND.");
                    exc.printStackTrace();
                }
                int k = ChordUtil.mapShaToRange(fileSha);
                insertFile(k,fileName);
                ChordUtil.createFile(fileName,path);
            }
        }
        this.server = new PeerServer(this);
        this.fix_finger = new FixFinger(this,range);
        //this.stabilize = new Stabilize(this,successorList);
    }

    public void notify(Data successor){
        if (successor!=null && !successor.getFingerAddress().equals(localAddress))
            ChordUtil.executeRemoteFunction(successor.getFingerAddress(),"IAMPRE_"+currNode.toString());
    }

    public void notified (Data newpre) {
        /*if (predecessor == null || predecessor.equals(localAddress)) {
            this.setPredecessor(newpre);
        }
        else {
            //Check Condition
            int newpre_relative_id = ChordUtil.calculateRelativeId(newpre.getFingerid(),predecessor.getFingerid(),this.range);
            int local_relative_id = ChordUtil.calculateRelativeId(localId,predecessor.getFingerid(),this.range);
            if (newpre_relative_id > 0 && newpre_relative_id < local_relative_id)
               this.setPredecessor(newpre);
        }*/
        this.setPredecessor(newpre);
    }

    public void initFingerTable(InetSocketAddress contact){
        String successor = ChordUtil.executeRemoteFunction(contact,"SUCFIND_"+((localId+1)%(1<<range)));
        finger.put(1,new Data(successor));
        predecessor = new Data(ChordUtil.executeRemoteFunction(this.getSuccessor().getFingerAddress(),"GETPREDECESSOR"));
        ChordUtil.executeRemoteFunction(this.getSuccessor().getFingerAddress(),"SETPRED_"+localId+"_"+localAddress);
        for(int i=2;i<=range;i++){
            int id = ((localId+(1<<(i-1)))%(1<<range));
            int query_id_relative = ChordUtil.calculateRelativeId(id,localId,this.range);
            int last_finger_relative = ChordUtil.calculateRelativeId(finger.get(i-1).getFingerid(),localId,this.range);
            System.out.println("Init i : "+i+" id = "+id+" query_id_relative : "+query_id_relative+" last_finger_relative : "+last_finger_relative);
            //Check Condition
            if(query_id_relative>0 && query_id_relative<=last_finger_relative){
                finger.put(i,finger.get(i-1));
                //updateIthFinger(finger.get(i-1),i);
            }else{
                String fingerEntry = ChordUtil.executeRemoteFunction(contact,"SUCFIND_"+id);
                finger.put(i,new Data(fingerEntry));
                //updateIthFinger(new Data(fingerEntry),i);
            }
        }
        System.out.println("Exiting Init .........");
    }

    public void transferKeys(){
        String fileList = ChordUtil.executeRemoteFunction(this.getSuccessor().getFingerAddress(),"TRANSFER_"+this.predecessor.getFingerid()+"_"+localId);
        //System.out.println("File Received :"+fileList);
        String fileArray[] = fileList.split("%");
        initFiles(fileArray,getSuccessor());
    }

    public void initFiles(String fileArray[],Data p){
        int fileId=-1;
        String fn="";
        for(String s:fileArray){
            if(s.isEmpty() || s==null){
                continue;
            }
            fileId = Integer.parseInt(s.substring(0,s.indexOf("_")));
            fn = s.substring(s.indexOf("_")+1);
            insertFile(fileId,fn);
            //Request Files Using a separate Connection
            ChordUtil.createFile(fn,path);
            String fc = ChordUtil.executeRemoteFunction(p.getFingerAddress(),"READ_"+fn);
            ChordUtil.writeFile(path,fn,fc);
        }
        System.out.println("All Files Transferred................");
    }

    public String sendFiles(int lower,int upper){
        String flist="";
        System.out.println("Transfer Upper = "+upper+" Lower = "+lower);
        Set<Integer> keys = files.keySet();
        ArrayList<Integer> nr = new ArrayList<>();
        nr.clear();
        for(Integer k:keys){
            if(lower<upper && k>lower && k<=upper){
                ArrayList<String> fl = files.get(k);
                for(String f:fl){
                    flist+=k+"_"+f+"%";
                }
                nr.add(k);
            }else if(upper<lower && (k>lower || k<=upper)){
                ArrayList<String> fl = files.get(k);
                for(String f:fl){
                    flist+=k+"_"+f+"%";
                }
                nr.add(k);
            }
        }
        for(Integer k:nr) files.remove(k);
        return  flist;
    }

    public void insertFile(int k,String fileName){
        if(files.containsKey(k)){
            ArrayList<String> list = files.get(k);
            list.add(fileName);
            files.put(k,list);
        }else{
            ArrayList<String> newList = new ArrayList<>();
            newList.add(fileName);
            files.put(k,newList);
        }
    }

    public Data findSuccessor(int id){
        Data n = this.findPredecesor(id);
        Data s = new Data(ChordUtil.executeRemoteFunction(n.getFingerAddress(),"GETSUC"));
        //System.out.println("Successor Returned : "+s.toString());
        return s;
    }

    public Data findPredecesor(int id){
        //Check Condition
        int query_id_relative=ChordUtil.calculateRelativeId(id,localId,this.range);
        int successor_relative = ChordUtil.calculateRelativeId(this.getSuccessor().getFingerid(),localId,this.range);
        //System.out.println("Find Predecessor :: The query_id : "+query_id_relative+" successor : "+successor_relative+" "+localId);
        if(query_id_relative>0 && query_id_relative<=successor_relative){
            //System.out.println("Entered If Condition");
            Data d = this.currNode;
            return d;
        }
        InetSocketAddress broadcastNode = this.closestPrecedingFinger(id);
        String broadcastIp = ChordUtil.executeRemoteFunction(broadcastNode,"FIND_"+id);
        Data result = null;
        try{
            result = new Data(broadcastIp);
        }catch(Exception exc){
            System.out.println("Unable to create address.");
            //exc.printStackTrace();
        }
        return result;
    }

    public InetSocketAddress closestPrecedingFinger(int id) {
        //Check Condition
        InetSocketAddress pred = this.getSuccessor().getFingerAddress();
        for (int i = range; i > 1; i--) {
            //Check Condition
            int finger_relative = ChordUtil.calculateRelativeId(finger.get(i).getFingerid(),localId,this.range);
            int id_relative = ChordUtil.calculateRelativeId(id,localId,this.range);
            if (finger_relative>0 && finger_relative < id_relative) {
                pred = finger.get(i).getFingerAddress();
                break;
            }
        }
        return pred;
    }

    public Data getSuccessor(){
        return finger.get(1);
    }

    public boolean updateOthers(){
        for(int i=1;i<=range;i++){
            int arg = localId-(1<<(i-1));
            arg = (arg<0)?(1<<range)+arg:arg;
            Data p = this.findPredecesor((arg)%(1<<range)),d=null;
            System.out.println("UpdateOthers "+p.toString()+" & i = "+i+" ID = "+localId);
            if(!p.getFingerAddress().equals(this.getLocalAddress()))
                d = new Data(ChordUtil.executeRemoteFunction(p.getFingerAddress(),"GETSUC"));
            else d=this.getSuccessor();
            int d_relative = ChordUtil.calculateRelativeId(d.getFingerid(),localId,5);
            int arg_relative = ChordUtil.calculateRelativeId(arg,localId,5);
            if(d_relative==arg_relative || d.getFingerid()==arg){
                p=d;
            }
            if(!p.getFingerAddress().equals(currNode.getFingerAddress()))
                ChordUtil.executeRemoteFunction(p.getFingerAddress(),"UPDATE_"+currNode.toString()+"_"+i);
        }
        return true;
    }

    public void updateFingerTable(Data p,int i){
        //Check Condition - p.getFingerid()>=localId && p.getFingerid()<finger.get(i).getFingerid()
        int p_relative = ChordUtil.calculateRelativeId(p.getFingerid(),localId,this.range);
        int finger_relative = ChordUtil.calculateRelativeId(finger.get(i).getFingerid(),localId,this.range);
        //Have Doubts regarding the MIT Papers
        if(p_relative>0 && p_relative<finger_relative){
            System.out.println("Updating Finger table entry : "+i+" "+p.toString());
            updateIthFinger(p,i);
            InetSocketAddress predecessorNode = this.predecessor.getFingerAddress();
            if(!p.getFingerAddress().equals(this.predecessor.getFingerAddress()))
                ChordUtil.executeRemoteFunction(predecessorNode,"UPDATE_"+p.toString()+"_"+i);
        }
    }

    synchronized public void updateIthFinger(Data p,int i){
        finger.put(i,p);
        if (i == 1 && p != null && !p.getFingerAddress().equals(localAddress)) {
            this.notify(p);
        }else if(i == 1 && p != null && p.getFingerAddress().equals(localAddress)){
            this.setPredecessor(p);
        }
    }

    public void deleteNode(){
        ChordUtil.executeRemoteFunction(getPredecessor().getFingerAddress(),"SETFINGER_"+1+"_"+getSuccessor().toString());
        //ChordUtil.executeRemoteFunction(getSuccessor().getFingerAddress(),"SETPRED_"+getPredecessor().toString());
        String str = "INSERTKEYS_";
        str+=getCurrNode().toString()+"&";
        Set<Integer> keys = files.keySet();
        for(int k:keys){
            ArrayList<String> flist=files.get(k);
            for(String f:flist){
                str+=k+"_"+f+"%";
            }
        }
        ChordUtil.executeRemoteFunction(getSuccessor().getFingerAddress(),str);
        //System.out.println("All Changes Made. God Help Me");
    }

    public void printFingerTable(){
        for(int i=1;i<=range;i++){
            System.out.println("i="+i+"["+((localId+(1<<(i-1)))%(1<<range))+","+((localId+(1<<(i)))%(1<<range))+") Finger="+finger.get(i));
        }
    }

    public void printInformation(){
        System.out.println("ID = "+ this.localId+ " IP = "+this.localAddress);
    }

    public void printSuccessor(){
        System.out.println("Successor ID = "+this.getSuccessor().getFingerid()+" IP = "+this.getSuccessor().getFingerAddress());
    }

    public void printPredecessor(){
        System.out.println("Predecessor ID = "+this.getPredecessor().getFingerid()+" IP = "+this.getPredecessor().getFingerAddress());
    }

    public void printFiles(){
        ArrayList<String> l;
        Set<Integer> keys = files.keySet();
        for(Integer k:keys){
            l = files.get(k);
            for(String f:l){
                System.out.println(k+" -> "+f);
            }
        }
    }

    public void stopAllThreads(){
        if (this.server != null)
            server.killServer();
        if (this.fix_finger != null)
            this.fix_finger.toDie();
    }
}

import java.net.InetSocketAddress;

public class Data {
    private int fingerid;
    private InetSocketAddress fingerAddress;
    public Data(int fingerid, InetSocketAddress fingerAddress) {
        this.fingerid = fingerid;
        this.fingerAddress = fingerAddress;
    }

    public Data(String node){
        this.fingerid = Integer.parseInt(node.substring(0,node.indexOf("_")));
        node = node.substring(node.indexOf("/")+1);
        this.fingerAddress = new InetSocketAddress(node.substring(0,node.indexOf(":")),Integer.parseInt(node.substring(node.indexOf(":")+1)));
    }
    public int getFingerid() {
        return fingerid;
    }

    public void setFingerid(int fingerid) {
        this.fingerid = fingerid;
    }

    public InetSocketAddress getFingerAddress() {
        return fingerAddress;
    }

    public void setFingerAddress(InetSocketAddress fingerAddress) {
        this.fingerAddress = fingerAddress;
    }

    public String toString(){
        return fingerid+"_"+fingerAddress.toString();
    }
}

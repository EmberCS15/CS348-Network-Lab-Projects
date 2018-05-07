import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer implements Runnable{
    private boolean alive;
    private ServerSocket peer;
    Thread t;
    private Node local;
    //Are u a puzzle or is puzzle u??????
    public PeerServer(Node local){
        this.local = local;
        try{
            this.peer = new ServerSocket(this.local.getLocalAddress().getPort());
        }catch(Exception exc){
            System.out.println("Peer Socket Failed ");
        }
        this.alive=true;
        t=new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while(this.alive){
            try{
                Socket incoming = peer.accept();
                new RemoteProcedure(local,incoming);
            }catch (Exception exc){
                System.out.println("Peer Could Not Accept Connection : ");
                exc.printStackTrace();
            }
        }
    }

    public void killServer(){
        this.alive=false;
    }
}


public class FixFinger implements Runnable{

    private Node local;
    private int range;
    private boolean alive;
    private int iter;
    Thread t;
    public FixFinger(Node node,int range) {
        local = node;
        alive = true;
        this.range=range;
        this.iter=0;
        t=new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while (alive) {
            iter=iter%range;
            int id = ((local.getLocalId()+(1<<(iter)))%(1<<range));
            Data ithfinger=null;
            try {
                ithfinger=local.findSuccessor(id);
            }catch(Exception exc){
                System.out.println("Could not compute Successor from finger table at Local Node : "+local.getLocalId()+" For ID : "+id);
                //exc.printStackTrace();
            }finally {
                //System.out.println("FixFinger : For ith entry : "+ithfinger.toString()+" id="+id+" iter = "+iter);
                if (ithfinger != null)
                    local.updateIthFinger(ithfinger, iter + 1);
                iter++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Thread Sleep Error in FixFinger1");
                    e.printStackTrace();
                }
            }
        }
    }

    public void toDie() {
        alive = false;
    }

}
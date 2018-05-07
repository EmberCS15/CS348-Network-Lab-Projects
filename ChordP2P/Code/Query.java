import java.net.InetSocketAddress;
import java.util.Scanner;

public class Query {
    private static InetSocketAddress localAddress;
    private static int localId;
    public static void main(String args[]) throws Exception{
        if(args.length != 1) {
            System.out.println("Need Required number of arguments");
            return;
        }
        // try to parse socket address from args, if fail, exit
        localAddress = ChordUtil.getInetSocketAddresss(args[0]);
        if (localAddress == null) {
            System.out.println("Cannot find address you are trying to contact. Now exit.");
            System.exit(0);;
        }

        // successfully constructed socket address of the node we are
        // trying to contact, check if it's alive
        String response = ChordUtil.executeRemoteFunction(localAddress,"UALIVE");

        // if it's dead, exit
        if (response == null || !response.equals("YES"))  {
            System.out.println("\nCannot find node you are trying to contact. Now exit.\n");
            System.exit(0);
        }
        String s = ChordUtil.getShaValue(localAddress.toString());
        localId = ChordUtil.mapShaToRange(s);
        // it's alive, print connection info
        System.out.println("Connection to node "+localAddress.getAddress().toString()+", port "+localAddress.getPort()+", position "+localId+".");

        // check if system is stable
        boolean pred = false;
        boolean succ = false;
        InetSocketAddress succ_addr = new Data(ChordUtil.executeRemoteFunction(localAddress,"GETSUC")).getFingerAddress();
        InetSocketAddress pred_addr = new Data(ChordUtil.executeRemoteFunction(localAddress, "GETPREDECESSOR")).getFingerAddress();
        if (pred_addr == null || succ_addr == null) {
            System.out.println("The node your are contacting is disconnected. Now exit.");
            System.exit(0);
        }
        if (pred_addr.equals(localAddress))
            pred = true;
        if (succ_addr.equals(localAddress))
            succ = true;

        // we suppose the system is stable if (1) this node has both valid
        // predecessor and successor or (2) none of them
        while (pred^succ) {
            System.out.println("Waiting for the system to be stable...");
            succ_addr = new Data(ChordUtil.executeRemoteFunction(localAddress,"GETSUC")).getFingerAddress();
            pred_addr = new Data(ChordUtil.executeRemoteFunction(localAddress,"GETPREDECESSOR")).getFingerAddress();
            if (pred_addr == null || succ_addr == null) {
                System.out.println("The node your are contacting is disconnected. Now exit.");
                System.exit(0);
            }
            if (pred_addr.equals(localAddress))
                pred = true;
            else
                pred = false;
            if (succ_addr.equals(localAddress))
                succ = true;
            else
                succ = false;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }

        // begin to take user input
        Scanner userinput = new Scanner(System.in);
        while(true) {
            System.out.println("\nPlease enter your search key (or type \"quit\" to leave): ");
            String command = null;
            command = userinput.nextLine();

            // quit
            if (command.startsWith("quit")) {
                System.exit(0);
            }

            // search
            else if (command.length() > 0){
                int hash = ChordUtil.mapShaToRange(ChordUtil.getShaValue(command));
                //System.out.println("\nHash value is "+Long.toHexString(hash));
                String str = ChordUtil.executeRemoteFunction(localAddress,"SUCFIND_"+hash);

                // if fail to send request, local node is disconnected, exit
                if (str == null) {
                    System.out.println("The node your are contacting is disconnected. Now exit.");
                    System.exit(0);
                }
                Data p = new Data(str);
                InetSocketAddress result = p.getFingerAddress();
                // print out response
                System.out.println("\nResponse from node "+localAddress.getAddress().toString()+", port "+localAddress.getPort());
                System.out.println("Node "+result.getAddress().toString()+", port "+result.getPort()+", position "+p.getFingerid());
            }
        }
    }
}

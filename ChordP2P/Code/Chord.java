import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chord {
    //First Argument is the IP Address of the known node and the second is the Port number of the node that is going to join
    public static void main(String args[]) throws Exception{
        if(args.length!=2){
            System.out.println("Enter the command line argument.Exiting.........");
            return;
        }
        int range = 5;
        Pattern p = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):(([0-9]){1,4}|[0-6][0-5][0-5][0-3][0-5])");
        Matcher m = p.matcher(args[0]);
        boolean b = m.matches();
        if(b){
            String localAddress = InetAddress.getLocalHost().getHostAddress();
            int port = Integer.parseInt(args[1]);
            InetSocketAddress localNodeIp = new InetSocketAddress(localAddress,port);
            Node mp_node = new Node(localNodeIp,range);
            InetSocketAddress mp_contact = ChordUtil.getInetSocketAddresss(args[0]);
            mp_node.join(mp_contact);

            Scanner sc = new Scanner(System.in);
            System.out.println("-------------------------COMMANDS---------------------------------");
            System.out.println("1.Print Finger Table\n2.Print Node Information\n3.Print Successor Information\n4.Print Predecessor Information\n5.Print Files\n6.Quit");
            while(true){
                System.out.println("\nPlease Enter your choice");
                int c = sc.nextInt();
                switch(c){
                    case 1:mp_node.printFingerTable();
                        break;
                    case 2:mp_node.printInformation();
                        break;
                    case 3:mp_node.printSuccessor();
                        break;
                    case 4:mp_node.printPredecessor();
                        break;
                    case 5:mp_node.printFiles();
                        break;
                    case 6:
                        mp_node.deleteNode();
                        mp_node.stopAllThreads();
                        System.out.println("Leaving the System.........");
                        System.exit(0);
                        break;
                    default:System.out.println("Please enter a valid Command");
                }
            }
        }else{
            System.out.println("Please enter a valid IP Address.\nExiting................");
            return;
        }
    }
}


/*------------------------TEST DATA------------------------------
TEST CASE 3
1. 172.16.179.236:4356 4356
2. 172.16.179.236:4356 7865

TEST CASE 1
1. 172.16.179.236:4356 4356
2. 172.16.179.236:4356 7822
3. 172.16.179.236:4356 2186
4. 172.16.179.236:4356 7632
5. 172.16.179.236:4356 5336


TEST CASE 2
6. 172.16.179.236:5336 1875
7. 172.16.179.236:5336 5134
5. 172.16.179.236:5336 5336 - 11
1. 172.16.179.236:5336 4356 - 3
2. 172.16.179.236:5336 7822 - 16
3. 172.16.179.236:5336 2186 - 29
4. 172.16.179.236:5336 7632 - 22

TEST CASE 3
1. 172.16.27.162:1233 1233 - 11
2. 172.16.27.162:1233 4122 - 3
3. 172.16.27.162:1233 1574 - 17
4. 172.16.27.162:1233 1995 - 28
5. 172.16.27.162:1233 8321 - 23


TEST CASE 4
1. 172.16.182.140:1154 1154 - 11
2. 172.16.182.140:1154 1131 - 3
3. 172.16.182.140:1154 1112 - 16
4. 172.16.182.140:1154 1206 - 29
5. 172.16.182.140:1154 1110 - 22
6. 172.16.182.140:1154 1138 - 4
7. 172.16.182.140:1154 1101 - 9
8. 172.16.182.140:1154 1102 - 8
9. 172.16.182.140:1154 1128 - 7
10.172.16.182.140:1154 1228 - 10
11.172.16.182.140:1154 1240 - 31
12.172.16.182.140:1154 1104 - 0
*/
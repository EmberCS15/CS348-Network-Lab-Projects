import objectclass.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Main {
    final private static int simulation_time=100;
    final private static int num_rounds=5000;
    static boolean link_occupied=false;
    static double outputTime=0;
    static void simulate(int n,PriorityQueue<Event> globalQueue,Link outputLink,Switch sw,double bss,int bitPacket,HashMap<String,Link> linkMap,HashMap<String,Source> sourceMap,double packetGenTime,long packetDropped,double lambda,String fileName)  throws Exception{
        FileWriter fw=new FileWriter(fileName);
        BufferedWriter bw=new BufferedWriter(fw);
        Random random=new Random();
        int i=0,j=0;
        for(j=1;j<=num_rounds;j++) {
            globalQueue.clear();
            outputLink.setBs(bss);
            double loadFactor = 0;
            double averageTime = 0;
            sw.setNumOfPackets(0);
            link_occupied=false;outputTime=0;
            packetDropped=0;
            //Initialising Global Priority Queue with n generation events
            for (i = 0; i < n; i++) {
                double t = random.nextDouble() * 0.5;
                Packet packetGenerated = new Packet("p" + i, "s" + i, bitPacket, t, 0.0);
                Event e = new Event("PACKET_GENERATED", t, packetGenerated);
                globalQueue.add(e);
            }
            //Simulation Loop running
            while (globalQueue.size()!=0) {
                Event e = globalQueue.poll(), temp1, temp2;
                //System.out.println("Event Type : "+e.getEventType()+" PacketId : "+e.getEventPacket().getPacketId()+" TimeStampp: "+e.getTimeStamp());
                double transRate = 0, queueOutTime = 0,timeSw=0;
                Packet pkt=null, packetGenerated=null;
                Link l=null;
                String id="";
                switch (e.getEventType()) {
                    case "PACKET_GENERATED":id = e.getEventPacket().getSrcId();
                        l = linkMap.get(sourceMap.get(id).getLinkId());
                        transRate = e.getEventPacket().getPktLength() / l.getBs();
                        temp1 = new Event("PACKET_QUEUE", e.getTimeStamp() + transRate, e.getEventPacket());
                        packetGenerated = new Packet("p" + i, e.getEventPacket().getSrcId(), bitPacket, e.getTimeStamp() + packetGenTime, 0.0);
                        temp2 = new Event("PACKET_GENERATED", e.getTimeStamp() + packetGenTime, packetGenerated);
                        globalQueue.add(temp1);
                        if (temp2.getTimeStamp() < simulation_time) {
                            globalQueue.add(temp2);
                            i++;
                        }
                        break;
                    case "PACKET_QUEUE":
                        if(sw.getNumOfPackets()>=sw.getMaxSize()){
                            packetDropped++;
                        }else{
                            pkt = e.getEventPacket();
                            //System.out.println("Queue Size for pkt:"+e.getEventPacket().getPacketId()+" is "+sw.getNumOfPackets()+" OutputLink :"+outputLink.getBs());
                            queueOutTime = Math.max(e.getTimeStamp(),outputTime) + ((sw.getNumOfPackets() * pkt.getPktLength())/outputLink.getBs());
                            if(link_occupied) queueOutTime-=(pkt.getPktLength()/outputLink.getBs());
                            temp1 = new Event("PACKET_DEQUEUE", queueOutTime , pkt);
                            globalQueue.add(temp1);
                            sw.incrementPacketQueueSize();
                        }
                        break;
                    case "PACKET_DEQUEUE":
                        pkt = e.getEventPacket();
                        transRate = (pkt.getPktLength() / outputLink.getBs());
                        timeSw = e.getTimeStamp()+transRate;
                        temp1 = new Event("PACKET_SWITCH", timeSw, pkt);
                        globalQueue.add(temp1);
                        link_occupied=true;
                        //System.out.println("Queue Size for pkt:"+e.getEventPacket().getPacketId()+" is "+sw.getNumOfPackets()+" Time Dequeue :"+e.getTimeStamp()+" Output Time : "+outputTime+" timeSw : "+timeSw);
                        outputTime = timeSw;
                        break;
                    case "PACKET_SWITCH":
                        link_occupied=false;
                        sw.decrementPacketQueueSize();
                        averageTime+=(e.getTimeStamp()-e.getEventPacket().getGenerationTime());
                        e.getEventPacket().setTerminationTime(e.getTimeStamp());
                        //System.out.println("Pkt .Id = "+e.getEventPacket().getPacketId()+" Gen Time: "+e.getEventPacket().getGenerationTime()+" Term Time: "+e.getEventPacket().getTerminationTime());
                        break;
                    default:System.out.println("Event ID MISMATCH ERROR.Process Exiting .......... ");
                        return;
                }
            }
            averageTime/=i;
            //packetDropped/=simulation_time;
            double rateOfLoss = packetDropped/(1.0*i);
            loadFactor=(n*lambda*bitPacket)/(bss);
            System.out.println("Round : "+j+" bss : "+bss+" averageTime : "+averageTime+" loadFactor : "+loadFactor+" packetDropped : "+packetDropped+" loss rate: "+rateOfLoss);
            if(sw.getMaxSize()==Integer.MAX_VALUE)
                bw.write(averageTime+","+loadFactor+"\n");
            else bw.write(rateOfLoss+","+loadFactor+"\n");
            bss+=10;
        }
        bw.close();
        fw.close();
    }
    public static void main(String args[]) throws Exception{
        //considering all to be 0 indexed
        System.out.println("-------------------------SIMULATION START----------------------------");
        Scanner sc=new Scanner(System.in);
        System.out.println("Enter the number of Sources .");
        int n=sc.nextInt();

        //Arrays Created
        HashMap<String,Source> sourceMap=new HashMap<String,Source>();
        HashMap<String,Link> linkMap=new HashMap<String,Link>();

        //Local variables
        int i=0,j=0,max_size=0;
        long packetDropped=0;
        double lambda=0,bs=0,bss=0,packetGenTime=0;
        int bitPacket=0;
        //creating the main priority queue..
        PriorityQueue<Event> globalQueue = new PriorityQueue<Event>(new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return (o1.getTimeStamp()<=o2.getTimeStamp())?-1:1;
            }
        });

        while(true){
            System.out.print("Enter the packet transfer rate of sources: ");
            lambda = sc.nextDouble();
            packetGenTime=(1.0/lambda);
            System.out.print("Enter the number of bits per packet sources:(Packet Length) ");
            bitPacket = sc.nextInt();
            System.out.print("Enter the BandWidth of link from sources (>"+(lambda*bitPacket)+" ) : ");
            bs = sc.nextDouble();
            if((lambda*bitPacket)>bs){
                System.out.println("Please enter an input where packet generation rate is tolerant with Transfer rate.(lambda*bitPacket)<bs");
            }else break;
        }

        for(i=0;i<n;i++){
            sourceMap.put("s"+i,new Source("s"+i,lambda,"l"+i));
            linkMap.put("l"+i,new Link("l"+i,bs,"s"+i,"SRC_SOURCE"));
        }

        //SWITCH and SWITCH LINK
        System.out.print("Enter then bandwidth of the switch output link (bss) : ");
        bss=sc.nextDouble();
        Link outputLink = new Link("ol1",bss,"sw1","SRC_SWITCH");
        System.out.println("Enter the maximum Size of Queue (For infinite queue enter a number less then or equal to 0)");
        max_size = sc.nextInt();
        Switch sw=new Switch("sw1",0,"ol1");
        String fileName="coordinates.txt";
        simulate(n,globalQueue,outputLink,sw,bss,bitPacket,linkMap,sourceMap,packetGenTime,packetDropped,lambda,fileName);
        fileName = "dropCoordinates.txt";
        globalQueue.clear();outputTime=0.0;link_occupied=false;packetDropped=0;
        sw.setMaxSize(max_size);outputLink.setBs(bss);
        simulate(n,globalQueue,outputLink,sw,bss,bitPacket,linkMap,sourceMap,packetGenTime,packetDropped,lambda,fileName);
        System.out.println("-------------------------------Simulation End------------------------------------");
        String[] callAndArgs= {"\"python\"","-u","\"plot.py\""};
        try {
            Process p = Runtime.getRuntime().exec(callAndArgs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

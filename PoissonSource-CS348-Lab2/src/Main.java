import objectclass.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Main {
    final private static int simulation_time=500;
    final private static int num_rounds=200,num_packet = 2000;
    static boolean link_occupied=false;
    static double outputTime=0;
    static double getNextTimeInterval(double lambda) throws Exception{
        int itr=0;
        double timeAvg = 0.0,R=0.0;
        for(itr=1;itr<=50;itr++){
            R = Math.random()*0.999999;
            R = (-1/lambda)*Math.log(1-R);
            timeAvg+=R;
        }
        timeAvg/=50;
        return timeAvg;
    }

    static void simulate(int n,PriorityQueue<Event> globalQueue,Link outputLink,Switch sw,double bss,int bitPacket,HashMap<String,Link> linkMap,HashMap<String,Source> sourceMap,long packetDropped,double lambda)  throws Exception{
        FileWriter fw1=new FileWriter("AverageDelayVsLamda.txt");
        BufferedWriter bw1=new BufferedWriter(fw1);
        FileWriter fw2=new FileWriter("AverageQueueVSLamda.txt");
        BufferedWriter bw2=new BufferedWriter(fw2);
        FileWriter fw3=new FileWriter("PoissonDistribution.txt");
        BufferedWriter bw3=new BufferedWriter(fw3);
        Random random=new Random();
        int i=0,j=0,max_pac = Integer.MIN_VALUE;
        int poissonArray[]=new int[simulation_time];
        for(j=1;j<=num_rounds;j++) {
            globalQueue.clear();
            outputLink.setBs(bss);
            max_pac = Integer.MIN_VALUE;
            double averageTime = 0,packetGenTime = 0.0,averageQueueSize=0.0;
            sw.setNumOfPackets(0);
            link_occupied=false;outputTime=0;
            packetDropped=0;
            Arrays.fill(poissonArray,0);
            //list.clear();
            //Initialising Global Priority Queue with n generation events
            for (i = 0; i < n; i++) {
                double t = random.nextDouble();
                Packet packetGenerated = new Packet("p" + i, "s" + i, bitPacket, t, 0.0);
                //sourceMap.get("s"+i).setNumOfPacketsGenerated(1);
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
                        packetGenTime=getNextTimeInterval(lambda);
                        packetGenerated = new Packet("p" + i, e.getEventPacket().getSrcId(), bitPacket, e.getTimeStamp() + packetGenTime, 0.0);
                        temp2 = new Event("PACKET_GENERATED", e.getTimeStamp() + packetGenTime, packetGenerated);
                        globalQueue.add(temp1);
                        if (temp2.getTimeStamp() < simulation_time /*&& i<num_packet*/) {
                            globalQueue.add(temp2);
                            poissonArray[(int)(Math.floor(e.getTimeStamp()))]++;
                            max_pac = Math.max(max_pac,poissonArray[(int)(Math.floor(e.getTimeStamp()))]);
                            //sourceMap.get(e.getEventPacket().getSrcId()).setNumOfPacketsGenerated(sourceMap.get(e.getEventPacket().getSrcId()).getNumOfPacketsGenerated()+1);
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
                            averageQueueSize+=sw.getNumOfPackets();
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
                        averageQueueSize+=sw.getNumOfPackets();
                        averageTime+=(e.getTimeStamp()-e.getEventPacket().getGenerationTime());
                        e.getEventPacket().setTerminationTime(e.getTimeStamp());
                        //System.out.println("Pkt .Id = "+e.getEventPacket().getPacketId()+" Gen Time: "+e.getEventPacket().getGenerationTime()+" Term Time: "+e.getEventPacket().getTerminationTime());
                        break;
                    default:System.out.println("Event ID MISMATCH ERROR.Process Exiting .......... ");
                        return;
                }
            }
            averageTime/=i;
            averageQueueSize/=(2*i);
            System.out.println("Round : "+j+" bss : "+bss+" averageTime : "+averageTime+" Lambda : "+lambda+" averageQueueSize : "+averageQueueSize);
            bw1.write(lambda+","+averageTime+"\n");
            bw2.write(lambda+","+averageQueueSize+"\n");
            lambda+=0.25;
        }
        int arr[]=new int[max_pac+3];
        for(j=0;j<simulation_time;j++){
            arr[poissonArray[j]]++;
        }
        for(j=0;j<arr.length;j++){
            bw3.write(j+","+arr[j]+"\n");
        }
        bw1.close();
        fw1.close();
        bw2.close();
        fw2.close();
        bw3.close();
        fw3.close();
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
        int i=0,j=0;
        long packetDropped=0;
        double lambda=0,bs=0,bss=0,m=Double.MAX_VALUE;
        int bitPacket=0,max_size=0;
        UniqueLambda ul = new UniqueLambda();
        //creating the main priority queue..
        PriorityQueue<Event> globalQueue = new PriorityQueue<Event>(new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return (o1.getTimeStamp()<=o2.getTimeStamp())?-1:1;
            }
        });
        System.out.print("Enter the number of bits per packet sources:(Packet Length) ");
        bitPacket = sc.nextInt();
        for(i=0;i<n;i++){
            while(true){
                System.out.print("Enter the packet transfer rate of sources "+i+": ");
                lambda = sc.nextDouble();
                System.out.print("Enter the BandWidth of link from sources (>"+(lambda*bitPacket)+" ) : ");
                bs = sc.nextDouble();
                if((lambda*bitPacket)>bs){
                    System.out.println("Please enter an input where packet generation rate is tolerant with Transfer rate.(lambda*bitPacket)<bs");
                }else break;
            }
            sourceMap.put("s"+i,new Source("s"+i,lambda,"l"+i));
            linkMap.put("l"+i,new Link("l"+i,bs,"s"+i,"SRC_SOURCE"));
            m=Math.min(m,bs);
        }
        System.out.println("Enter the common lambda < ("+(m/bitPacket)+") : ");
        lambda = sc.nextDouble();
        //SWITCH and SWITCH LINK
        System.out.print("Enter then bandwidth of the switch output link (bss) : ");
        bss=sc.nextDouble();
        System.out.print("Enter the maximum size of Queue : ");
        max_size=sc.nextInt();
        Link outputLink = new Link("ol1",bss,"sw1","SRC_SWITCH");
        Switch sw=new Switch("sw1",0,"ol1");
        simulate(n,globalQueue,outputLink,sw,bss,bitPacket,linkMap,sourceMap,packetDropped,lambda);
        System.out.println("Unique lambda Average number of packet drop/delay per source simulation.");
        globalQueue.clear();
        String fileName="AverageDelayPerSource.txt";
        ul.packetDropSimulate(n,globalQueue,outputLink,sw,bss,bitPacket,linkMap,sourceMap,fileName,num_rounds,simulation_time);
        sw.setMaxSize(max_size);
        globalQueue.clear();
        fileName="AveragePacketDropPerSource.txt";
        ul.packetDropSimulate(n,globalQueue,outputLink,sw,bss,bitPacket,linkMap,sourceMap,fileName,num_rounds,simulation_time);
        System.out.println("-------------------------------Simulation End------------------------------------");
        String[] callAndArgs= {"\"python\"","-u","\"plot.py\""};
        try {
            Process p = Runtime.getRuntime().exec(callAndArgs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
4
2
10 30
10 30
4 20
8 20
7
100
3


4
10
10 120
8.7 90
6.9 75
9.8 100
450
5

4
12000
0.1 384000
0.5 384000
4 384000
16 384000
0.4
200000
20

4
12000
0.9 384000
2 384000
4 384000
16 384000
0.4
250000
10

*/
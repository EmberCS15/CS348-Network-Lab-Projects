import objectclass.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

public class UniqueLambda {
    static boolean link_occupied=false;
    static double outputTime=0;
    private static class Data{
        double packetDrop;
        double packetDelay;
        public Data(double packetDrop,double packetDelay){
            this.packetDrop=packetDrop;
            this.packetDelay = packetDelay;
        }
    }
    static double getNextTimeInterval(double lambda){
        int itr=0;
        double timeAvg = 0.0,R=0.0;
        for(itr=1;itr<=50;itr++){
            R = Math.random();
            R = (-1/lambda)*Math.log(1-R);
            timeAvg+=R;
        }
        timeAvg/=50;
        //System.out.println("Time : "+timeAvg);
        return timeAvg;
    }
    static void packetDropSimulate(int n, PriorityQueue<Event> globalQueue, Link outputLink, Switch sw, double bss, int bitPacket, HashMap<String,Link> linkMap, HashMap<String, Source> sourceMap,String filename,final int num_rounds,final int simulation_time) throws Exception{
        FileWriter fw1=new FileWriter(filename);
        BufferedWriter bw1=new BufferedWriter(fw1);
        Random random=new Random();
        int i=0,j=0,k=0;
        Data srcArray[][]=new Data[n][num_rounds];
        for(i=0;i<n;i++){
            for(j=0;j<num_rounds;j++){
                srcArray[i][j]=new Data(0,0.0);
            }
        }
        for(j=0;j<num_rounds;j++) {
            globalQueue.clear();
            outputLink.setBs(bss);
            sw.setNumOfPackets(0);
            link_occupied=false;outputTime=0;
            //Initialising Global Priority Queue with n generation events
            for (i = 0; i < n; i++) {
                double t = random.nextDouble();
                Packet packetGenerated = new Packet("p" + i, "s" + i, bitPacket, t, 0.0);
                sourceMap.get("s"+i).setNumOfPacketsGenerated(1);
                Event e = new Event("PACKET_GENERATED", t, packetGenerated);
                globalQueue.add(e);
            }
            //Simulation Loop running
            while (globalQueue.size()!=0) {
                Event e = globalQueue.poll(), temp1, temp2;
                //System.out.println("Event Type : "+e.getEventType()+" PacketId : "+e.getEventPacket().getPacketId()+" TimeStampp: "+e.getTimeStamp());
                double transRate = 0, queueOutTime = 0,timeSw=0,packetGenTime=0.0;
                Packet pkt=null, packetGenerated=null;
                Link l=null;
                Source src = null;
                String id="";
                switch (e.getEventType()) {
                    case "PACKET_GENERATED":id = e.getEventPacket().getSrcId();
                        l = linkMap.get(sourceMap.get(id).getLinkId());
                        transRate = e.getEventPacket().getPktLength() / l.getBs();
                        src = sourceMap.get(id);
                        temp1 = new Event("PACKET_QUEUE", e.getTimeStamp() + transRate, e.getEventPacket());
                        packetGenTime = getNextTimeInterval(src.getPacketSendingRate());
                        packetGenerated = new Packet("p" + i, e.getEventPacket().getSrcId(), bitPacket, e.getTimeStamp() + packetGenTime, 0.0);
                        temp2 = new Event("PACKET_GENERATED", e.getTimeStamp() + packetGenTime, packetGenerated);
                        globalQueue.add(temp1);
                        if (temp2.getTimeStamp() < simulation_time) {
                            globalQueue.add(temp2);
                            sourceMap.get(e.getEventPacket().getSrcId()).setNumOfPacketsGenerated(sourceMap.get(e.getEventPacket().getSrcId()).getNumOfPacketsGenerated()+1);
                            i++;
                        }
                        break;
                    case "PACKET_QUEUE":
                        if(sw.getNumOfPackets()>=sw.getMaxSize()){
                            //System.out.println("I am greater then = "+sw.getMaxSize());
                            srcArray[Integer.parseInt(e.getEventPacket().getSrcId().substring(1))][j].packetDrop+=1;
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
                        e.getEventPacket().setTerminationTime(e.getTimeStamp());
                        srcArray[Integer.parseInt(e.getEventPacket().getSrcId().substring(1))][j].packetDelay+=(e.getTimeStamp()-e.getEventPacket().getGenerationTime());
                        if(e.getEventPacket().getGenerationTime()>e.getEventPacket().getTerminationTime())
                            System.out.println("Pkt .Id = "+e.getEventPacket().getPacketId()+" Gen Time: "+e.getEventPacket().getGenerationTime()+" Term Time: "+e.getEventPacket().getTerminationTime());
                        break;
                    default:System.out.println("Event ID MISMATCH ERROR.Process Exiting .......... ");
                        return;
                }
            }
            for(k=0;k<n;k++){
                //System.out.println("Delay : "+srcArray[k][j].packetDelay);
                srcArray[k][j].packetDelay/=sourceMap.get("s"+k).getNumOfPacketsGenerated();
                srcArray[k][j].packetDrop/=sourceMap.get("s"+k).getNumOfPacketsGenerated();
            }
        }
        for(j=0;j<n;j++){
            for(k=0;k<num_rounds;k++){
                if(sw.getMaxSize()==Integer.MAX_VALUE) bw1.write(j+","+srcArray[j][k].packetDelay+"\n");
                else bw1.write(j+","+srcArray[j][k].packetDrop+"\n");
                //System.out.println("Source : "+j+" Average Packet Delay : "+srcArray[j][k].packetDelay+" Average Packet Drop : "+srcArray[j][k].packetDrop);
            }
        }
        bw1.close();
        fw1.close();
    }
}

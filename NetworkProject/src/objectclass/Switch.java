package objectclass;

import java.util.LinkedList;

public class Switch {
    private String switchId;
    private int numOfPackets;
    private int maxSize;
    private String linkId;

    public Switch(String switchId, int numOfPackets,String linkId) {
        this.switchId = switchId;
        this.numOfPackets = numOfPackets;
        this.maxSize=Integer.MAX_VALUE;
        this.linkId=linkId;
    }

    public Switch(String switchId, int numOfPackets, int maxSize,String linkId) {
        this.switchId = switchId;
        this.numOfPackets = numOfPackets;
        this.maxSize = maxSize;
        this.linkId = linkId;
    }

    public String getSwitchId() {
        return switchId;
    }

    public void setSwitchId(String switchId) {
        this.switchId = switchId;
    }

    public int getNumOfPackets() {
        return this.numOfPackets;
    }

    public void incrementPacketQueueSize(){
        this.numOfPackets++;
    }

    public void decrementPacketQueueSize(){
        this.numOfPackets--;
    }

    public void setNumOfPackets(int value){
        this.numOfPackets=value;
    }
    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }
}

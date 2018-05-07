package objectclass;

public class Source {
    //Source Ids as s1,s2,....
    private String srcId;
    private double packetSendingRate;
    //link Ids as l1,l2,l3...... . This should actually be an array if we consider a real life network
    private String linkId;

    public Source(String srcId, double packetSendingRate, String linkId) {
        this.srcId = srcId;
        this.packetSendingRate = packetSendingRate;
        this.linkId = linkId;
    }

    public String getSrcId() {
        return srcId;
    }

    public double getPacketSendingRate() {
        return packetSendingRate;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public void setPacketSendingRate(double packetSendingRate) {
        this.packetSendingRate = packetSendingRate;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }
}

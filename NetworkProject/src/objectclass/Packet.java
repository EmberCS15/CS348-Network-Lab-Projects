package objectclass;

public class Packet {
    //packet ids should be of the form of p1,p2,p3.....
    private String packetId;
    private String srcId;
    private int pktLength;
    private double generationTime;
    private double terminationTime;

    public Packet(String packetId, String srcId, int pktLength, double generationTime, double terminationTime) {
        this.packetId = packetId;
        this.srcId = srcId;
        this.pktLength = pktLength;
        this.generationTime = generationTime;
        this.terminationTime = terminationTime;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public int getPktLength() {
        return pktLength;
    }

    public void setPktLength(int pktLength) {
        this.pktLength = pktLength;
    }

    public double getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(double generationTime) {
        this.generationTime = generationTime;
    }

    public double getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(double terminationTime) {
        this.terminationTime = terminationTime;
    }
}

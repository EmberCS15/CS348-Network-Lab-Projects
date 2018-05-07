package objectclass;

public class Event {
    private String eventType;
    private double timeStamp;
    private Packet pkt;

    public Event(String eventType, double timeStamp, Packet pkt) {
        this.eventType = eventType;
        this.timeStamp = timeStamp;
        this.pkt = pkt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Packet getEventPacket() {
        return pkt;
    }

    public void setEventPacket(Packet pkt) {
        this.pkt = pkt;
    }
}

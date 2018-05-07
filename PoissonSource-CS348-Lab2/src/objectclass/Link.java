package objectclass;

public class Link {
    final private String sourceTypeArray[]={"SRC_SWITCH","SRC_SOURCE"};
    private String linkID;
    private double bs;
    private String srcId;
    //This is generated from the above array
    private String srcType;
    public Link(String linkID, double bs, String srcId, String srcType) {
        this.linkID = linkID;
        this.bs = bs;
        this.srcId = srcId;
        this.srcType = srcType;
    }

    public String[] getSourceTypeArray() {
        return sourceTypeArray;
    }

    public String getLinkID() {
        return linkID;
    }

    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }

    public double getBs() {
        return bs;
    }

    public void setBs(double bs) {
        this.bs = bs;
    }

    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public String getSrcType() {
        return srcType;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }
}

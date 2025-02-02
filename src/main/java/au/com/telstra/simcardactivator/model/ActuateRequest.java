package au.com.telstra.simcardactivator.model;

public class ActuateRequest {
    private String iccid;

    public ActuateRequest() {}
    public ActuateRequest(String iccid) {
        this.iccid = iccid;
    }

    public String getIccid() { return iccid; }
    public void setIccid(String iccid) { this.iccid = iccid; }
}

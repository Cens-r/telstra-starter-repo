package au.com.telstra.simcardactivator.model;

public class TaskResponse {
    private boolean success;

    public TaskResponse() {}
    public TaskResponse(boolean success) {
        this.success = success;
    }

    public boolean getSuccess() { return this.success; }
    public void setSuccess(boolean success) { this.success = success; }
}

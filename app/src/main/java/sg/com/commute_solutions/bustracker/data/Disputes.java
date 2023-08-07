package sg.com.commute_solutions.bustracker.data;

/**
 * Created by Kyle on 22/6/17.
 */

public class Disputes {
    private String id;
    private String accessCode;
    private String details;
    private double cost;
    private boolean requiresAction;
    private boolean isSettled;
    private boolean pendingAdmin;
    private String rejectedReason;
    private String disputeReasons;
    private String adminInput;

    public Disputes(String accessCode, String details, double cost, boolean requiresAction, boolean isSettled, boolean pendingAdmin, String id, String rejectedReason, String disputeReasons, String adminInput) {
        this.accessCode = accessCode;
        this.details = details;
        this.cost = cost;
        this.requiresAction = requiresAction;
        this.isSettled = isSettled;
        this.pendingAdmin = pendingAdmin;
        this.id = id;
        this.rejectedReason = rejectedReason;
        this.disputeReasons = disputeReasons;
        this.adminInput = adminInput;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isRequiresAction() {
        return requiresAction;
    }

    public void setRequiresAction(boolean requiresAction) {
        this.requiresAction = requiresAction;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }

    public boolean isPendingAdmin() {
        return pendingAdmin;
    }

    public void setPendingAdmin(boolean pendingAdmin) {
        this.pendingAdmin = pendingAdmin;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public String getDisputeReasons() {
        return disputeReasons;
    }

    public void setDisputeReasons(String disputeReasons) {
        this.disputeReasons = disputeReasons;
    }

    public String getAdminInput() {
        return adminInput;
    }

    public void setAdminInput(String adminInput) {
        this.adminInput = adminInput;
    }
}

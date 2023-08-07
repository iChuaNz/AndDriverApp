package sg.com.commute_solutions.bustracker.data;

/**
 * Created by Kyle on 23/5/17.
 */

public class TransactionHistory {
    private String id;
    private String date;
    private boolean isWithdraw;
    private String transactionDetails;
    private String serviceType;
    private String transactionAmount;
    private String transactionMethod;

    public TransactionHistory(String id, String date, boolean isWithdraw, String transactionDetails, String serviceType, String transactionAmount, String transactionMethod) {
        this.id = id;
        this.date = date;
        this.isWithdraw = isWithdraw;
        this.transactionDetails = transactionDetails;
        this.serviceType = serviceType;
        this.transactionAmount = transactionAmount;
                            this.transactionMethod = transactionMethod;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isWithdraw() {
        return isWithdraw;
    }

    public void setWithdraw(boolean withdraw) {
        isWithdraw = withdraw;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionMethod() {
        return transactionMethod;
    }

    public void setTransactionMethod(String transactionMethod) {
        this.transactionMethod = transactionMethod;
    }
}

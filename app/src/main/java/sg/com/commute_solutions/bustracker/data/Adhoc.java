package sg.com.commute_solutions.bustracker.data;

/**
 * Created by Kyle on 5/6/17.
 */

public class Adhoc {
    private String id;
    private int jobStatus;

    private String pocName;
    private String pocContactNo;

    public Adhoc(String id, int jobStatus, String pocName, String pocContactNo) {
        this.id = id;
        this.jobStatus = jobStatus;
        this.pocName = pocName;
        this.pocContactNo = pocContactNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(int jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getPocName() {
        return pocName;
    }

    public void setPocName(String pocName) {
        this.pocName = pocName;
    }

    public String getPocContactNo() {
        return pocContactNo;
    }

    public void setPocContactNo(String pocContactNo) {
        this.pocContactNo = pocContactNo;
    }
}

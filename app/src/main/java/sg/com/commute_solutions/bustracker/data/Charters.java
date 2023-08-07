package sg.com.commute_solutions.bustracker.data;

/**
 * Created by Kyle on 5/4/17.
 */

public class Charters {
    private String id;
    private String date;
    private String[] pickupName;
    private String[] dropOffName;
    private String[] time;
    private String cost;
    private int disposalDuration;
    private String expiryDate;
    private String[] pickupLatitude;
    private String[] pickupLongitude;
    private String[] dropOffLatitude;
    private String[] dropOffLongitude;
    private int busType;
    private String remark;
    private String pocName;
    private String pocEmail;
    private String pocContactNo;
    private String pocCompany;
    private String accessCode;
    private int busQuantity;
    private boolean isMyCharter;
    private String serviceType;
    private String[] pickUpTime;
    private String[] dropOffTime;
    private String driverName;
    private String driverContactNo;
    private String vehicleNo;
    private boolean isAccepted;
    private boolean isCancelled;
    private boolean isDisputable;
    private boolean canResubmit;
    private boolean isCompleted;
    private String trackingUrl;

    public Charters(String id, String serviceType, String date, String[] pickupName, String[] dropOffName, String[] time, String cost, int disposalDuration,
                    String expiryDate, String[] pickupLatitude, String[] pickupLongitude, String[] dropOffLatitude, String[] dropOffLongitude, int busType,
                    String remark, String accessCode, boolean isMyCharter, boolean canResubmit) {

        this.id = id;
        this.serviceType = serviceType;
        this.date = date;
        this.pickupName = pickupName;
        this.dropOffName = dropOffName;
        this.time = time;
        this.cost = cost;
        this.disposalDuration = disposalDuration;
        this.expiryDate = expiryDate;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropOffLatitude = dropOffLatitude;
        this.dropOffLongitude = dropOffLongitude;
        this.busType = busType;
        this.remark = remark;
        this.accessCode = accessCode;
        this.isMyCharter = isMyCharter;
        this.canResubmit = canResubmit;
    }

    public Charters(String id, String date, String[] pickupName, String[] dropOffName, String[] time, String cost, int disposalDuration,
                    String expiryDate, String[] pickupLatitude, String[] pickupLongitude, String[] dropOffLatitude, String[] dropOffLongitude, int busType,
                    String remark, String pocName, String pocEmail, String pocContactNo, String pocCompany, String accessCode, String driverName,
                    String driverContactNo, String vehicleNo, boolean isCancelled, boolean isDisputable, boolean isCompleted, String trackingUrl, boolean canResubmit) {

        this.id = id;
        this.date = date;
        this.pickupName = pickupName;
        this.dropOffName = dropOffName;
        this.time = time;
        this.cost = cost;
        this.disposalDuration = disposalDuration;
        this.expiryDate = expiryDate;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropOffLatitude = dropOffLatitude;
        this.dropOffLongitude = dropOffLongitude;
        this.busType = busType;
        this.remark = remark;
        this.pocName = pocName;
        this.pocEmail = pocEmail;
        this.pocContactNo = pocContactNo;
        this.pocCompany = pocCompany;
        this.accessCode = accessCode;
        this.driverName = driverName;
        this.driverContactNo = driverContactNo;
        this.vehicleNo = vehicleNo;
        this.isCancelled = isCancelled;
        this.isDisputable = isDisputable;
        this.isCompleted = isCompleted;
        this.trackingUrl = trackingUrl;
        this.canResubmit = canResubmit;
    }

    public Charters(String id, String date, String[] pickupName, String[] dropOffName, String[] time, String cost, int disposalDuration,
                    String expiryDate, String[] pickupLatitude, String[] pickupLongitude, String[] dropOffLatitude, String[] dropOffLongitude, int busType,
                    String remark, String pocName, String pocEmail, String pocContactNo, String pocCompany, String accessCode, String driverName,
                    String driverContactNo, String vehicleNo, boolean isAccepted, boolean isCancelled, boolean isDisputable, boolean isCompleted, String trackingUrl,
                    boolean canResubmit) {

        this.id = id;
        this.date = date;
        this.pickupName = pickupName;
        this.dropOffName = dropOffName;
        this.time = time;
        this.cost = cost;
        this.disposalDuration = disposalDuration;
        this.expiryDate = expiryDate;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropOffLatitude = dropOffLatitude;
        this.dropOffLongitude = dropOffLongitude;
        this.busType = busType;
        this.remark = remark;
        this.pocName = pocName;
        this.pocEmail = pocEmail;
        this.pocContactNo = pocContactNo;
        this.pocCompany = pocCompany;
        this.accessCode = accessCode;
        this.driverName = driverName;
        this.driverContactNo = driverContactNo;
        this.vehicleNo = vehicleNo;
        this.isAccepted = isAccepted;
        this.isCancelled = isCancelled;
        this.isDisputable = isDisputable;
        this.isCompleted = isCompleted;
        this.trackingUrl = trackingUrl;
        this.canResubmit =canResubmit;
    }

    public Charters(String serviceType, String[] pickupName, String[] dropOffName, String[] pickUpTime, String[] dropOffTime, String cost, int disposalDuration,
                    String[] pickupLatitude, String[] pickupLongitude, String[] dropOffLatitude, String[] dropOffLongitude, int busType,
                    String remark, int busQuantity) {

        this.serviceType = serviceType;
        this.pickupName = pickupName;
        this.dropOffName = dropOffName;
        this.pickUpTime = pickUpTime;
        this.dropOffTime = dropOffTime;
        this.cost = cost;
        this.disposalDuration = disposalDuration;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropOffLatitude = dropOffLatitude;
        this.dropOffLongitude = dropOffLongitude;
        this.busType = busType;
        this.remark = remark;
        this.busQuantity = busQuantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String[] getPickupName() {
        return pickupName;
    }

    public void setPickupName(String[] pickupName) {
        this.pickupName = pickupName;
    }

    public String[] getDropOffName() {
        return dropOffName;
    }

    public void setDropOffName(String[] dropOffName) {
        this.dropOffName = dropOffName;
    }

    public String[] getTime() {
        return time;
    }

    public void setTime(String[] time) {
        this.time = time;
    }

    public boolean isMyCharter() {
        return isMyCharter;
    }

    public void setMyCharter(boolean myCharter) {
        isMyCharter = myCharter;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public int getDisposalDuration() {
        return disposalDuration;
    }

    public void setDisposalDuration(int disposalDuration) {
        this.disposalDuration = disposalDuration;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String[] getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(String[] pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public String[] getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(String[] pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public String[] getDropOffLatitude() {
        return dropOffLatitude;
    }

    public void setDropOffLatitude(String[] dropOffLatitude) {
        this.dropOffLatitude = dropOffLatitude;
    }

    public String[] getDropOffLongitude() {
        return dropOffLongitude;
    }

    public void setDropOffLongitude(String[] dropOffLongitude) {
        this.dropOffLongitude = dropOffLongitude;
    }

    public int getBusType() {
        return busType;
    }

    public void setBusType(int busType) {
        this.busType = busType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPocName() {
        return pocName;
    }

    public void setPocName(String pocName) {
        this.pocName = pocName;
    }

    public String getPocEmail() {
        return pocEmail;
    }

    public void setPocEmail(String pocEmail) {
        this.pocEmail = pocEmail;
    }

    public String getPocContactNo() {
        return pocContactNo;
    }

    public void setPocContactNo(String pocContactNo) {
        this.pocContactNo = pocContactNo;
    }

    public String getPocCompany() {
        return pocCompany;
    }

    public void setPocCompany(String pocCompany) {
        this.pocCompany = pocCompany;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public int getBusQuantity() {
        return busQuantity;
    }

    public void setBusQuantity(int busQuantity) {
        this.busQuantity = busQuantity;
    }

    public String[] getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(String[] pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public String[] getDropOffTime() {
        return dropOffTime;
    }

    public void setDropOffTime(String[] dropOffTime) {
        this.dropOffTime = dropOffTime;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverContactNo() {
        return driverContactNo;
    }

    public void setDriverContactNo(String driverContactNo) {
        this.driverContactNo = driverContactNo;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public boolean isDisputable() {
        return isDisputable;
    }

    public void setDisputable(boolean disputable) {
        isDisputable = disputable;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isCanResubmit() {
        return canResubmit;
    }

    public void setCanResubmit(boolean canResubmit) {
        this.canResubmit = canResubmit;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }
}

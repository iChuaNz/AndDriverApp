package sg.com.commute_solutions.bustracker.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Kyle on 16/1/18.
 */

public class Contract {
    private String id;
    private String pickup1Name;
    private String pickup2Name;
    private String pickup3Name;
    private LatLng pickup1LatLng;
    private LatLng pickup2LatLng;
    private LatLng pickup3LatLng;
    private String dropoff1Name;
    private String dropoff2Name;
    private String dropoff3Name;
    private LatLng dropoff1LatLng;
    private LatLng dropoff2LatLng;
    private LatLng dropoff3LatLng;
    private boolean hasERP;
    private String startDate;
    private String endDate;
    private String pickupTime;
    private String contactNo;
    private Double costPerMonth;
    private String costPerMonthInString;
    private Boolean includesERP;
    private String busSize;
    private String additionalInfo;
    private boolean isOwnCharter;

    public Contract (String pickup1Name, String pickup2Name, String pickup3Name, LatLng pickup1LatLng, LatLng pickup2LatLng, LatLng pickup3LatLng,
                     String dropoff1Name, String dropoff2Name, String dropoff3Name, LatLng dropoff1LatLng, LatLng dropoff2LatLng, LatLng dropoff3LatLng,
                     boolean hasERP) {
        this.pickup1Name = pickup1Name;
        this.pickup2Name = pickup2Name;
        this.pickup3Name = pickup3Name;
        this.pickup1LatLng = pickup1LatLng;
        this.pickup2LatLng = pickup2LatLng;
        this.pickup3LatLng = pickup3LatLng;
        this.dropoff1Name = dropoff1Name;
        this.dropoff2Name = dropoff2Name;
        this.dropoff3Name = dropoff3Name;
        this.dropoff1LatLng = dropoff1LatLng;
        this.dropoff2LatLng = dropoff2LatLng;
        this.dropoff3LatLng = dropoff3LatLng;
        this.hasERP = hasERP;
    }

    public Contract (String id, String pickup1Name, String pickup2Name, String pickup3Name, LatLng pickup1LatLng, LatLng pickup2LatLng, LatLng pickup3LatLng,
                     String dropoff1Name, String dropoff2Name, String dropoff3Name, LatLng dropoff1LatLng, LatLng dropoff2LatLng, LatLng dropoff3LatLng,
                     boolean hasERP, String startDate, String endDate, String pickupTime, String contactNo, String costPerMonthInString, Boolean includesERP,
                     String busSize, String additionalInfo, boolean isOwnCharter) {
        this.id = id;
        this.pickup1Name = pickup1Name;
        this.pickup2Name = pickup2Name;
        this.pickup3Name = pickup3Name;
        this.pickup1LatLng = pickup1LatLng;
        this.pickup2LatLng = pickup2LatLng;
        this.pickup3LatLng = pickup3LatLng;
        this.dropoff1Name = dropoff1Name;
        this.dropoff2Name = dropoff2Name;
        this.dropoff3Name = dropoff3Name;
        this.dropoff1LatLng = dropoff1LatLng;
        this.dropoff2LatLng = dropoff2LatLng;
        this.dropoff3LatLng = dropoff3LatLng;
        this.hasERP = hasERP;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pickupTime = pickupTime;
        this.contactNo = contactNo;
        this.costPerMonthInString = costPerMonthInString;
        this.includesERP = includesERP;
        this.busSize = busSize;
        this.additionalInfo = additionalInfo;
        this.isOwnCharter = isOwnCharter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPickup1Name() {
        return pickup1Name;
    }

    public void setPickup1Name(String pickup1Name) {
        this.pickup1Name = pickup1Name;
    }

    public String getPickup2Name() {
        return pickup2Name;
    }

    public void setPickup2Name(String pickup2Name) {
        this.pickup2Name = pickup2Name;
    }

    public String getPickup3Name() {
        return pickup3Name;
    }

    public void setPickup3Name(String pickup3Name) {
        this.pickup3Name = pickup3Name;
    }

    public LatLng getPickup1LatLng() {
        return pickup1LatLng;
    }

    public void setPickup1LatLng(LatLng pickup1LatLng) {
        this.pickup1LatLng = pickup1LatLng;
    }

    public LatLng getPickup2LatLng() {
        return pickup2LatLng;
    }

    public void setPickup2LatLng(LatLng pickup2LatLng) {
        this.pickup2LatLng = pickup2LatLng;
    }

    public LatLng getPickup3LatLng() {
        return pickup3LatLng;
    }

    public void setPickup3LatLng(LatLng pickup3LatLng) {
        this.pickup3LatLng = pickup3LatLng;
    }

    public String getDropoff1Name() {
        return dropoff1Name;
    }

    public void setDropoff1Name(String dropoff1Name) {
        this.dropoff1Name = dropoff1Name;
    }

    public String getDropoff2Name() {
        return dropoff2Name;
    }

    public void setDropoff2Name(String dropoff2Name) {
        this.dropoff2Name = dropoff2Name;
    }

    public String getDropoff3Name() {
        return dropoff3Name;
    }

    public void setDropoff3Name(String dropoff31Name) {
        this.dropoff3Name = dropoff31Name;
    }

    public LatLng getDropoff1LatLng() {
        return dropoff1LatLng;
    }

    public void setDropoff1LatLng(LatLng dropoff1LatLng) {
        this.dropoff1LatLng = dropoff1LatLng;
    }

    public LatLng getDropoff2LatLng() {
        return dropoff2LatLng;
    }

    public void setDropoff2LatLng(LatLng dropoff2LatLng) {
        this.dropoff2LatLng = dropoff2LatLng;
    }

    public LatLng getDropoff3LatLng() {
        return dropoff3LatLng;
    }

    public void setDropoff3LatLng(LatLng dropoff3LatLng) {
        this.dropoff3LatLng = dropoff3LatLng;
    }

    public boolean isHasERP() {
        return hasERP;
    }

    public void setHasERP(boolean hasERP) {
        this.hasERP = hasERP;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public Double getCostPerMonth() {
        return costPerMonth;
    }

    public void setCostPerMonth(Double costPerMonth) {
        this.costPerMonth = costPerMonth;
    }

    public String getCostPerMonthInString() {
        return costPerMonthInString;
    }

    public void setCostPerMonthInString(String costPerMonthInString) {
        this.costPerMonthInString = costPerMonthInString;
    }

    public Boolean getIncludesERP() {
        return includesERP;
    }

    public void setIncludesERP(Boolean includesERP) {
        this.includesERP = includesERP;
    }

    public String getBusSize() {
        return busSize;
    }

    public void setBusSize(String busSize) {
        this.busSize = busSize;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public boolean isOwnCharter() {
        return isOwnCharter;
    }

    public void setOwnCharter(boolean ownCharter) {
        isOwnCharter = ownCharter;
    }
}

package sg.com.commute_solutions.bustracker.data;

/**
 * Created by Kyle on 29/9/16.
 */
public class RoutePoint {
    private String pointName;
    private Double longitude;
    private Double latitude;
    private Integer type;
    private String time;
    private Integer numberOfPassengers;

    public RoutePoint(String pointName, Double longitude, Double latitude, Integer type, String time, Integer numberOfPassengers) {
        this.pointName = pointName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = type;
        this.time = time;
        this.numberOfPassengers = numberOfPassengers;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(Integer numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }
}

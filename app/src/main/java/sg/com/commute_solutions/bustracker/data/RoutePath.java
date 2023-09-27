package sg.com.commute_solutions.bustracker.data;

/**
 * Created by Kyle on 29/9/16.
 */
public class RoutePath {
    private Double longitude;
    private Double latitude;

    public RoutePath(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
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
}

package sg.com.commute_solutions.bustracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle on 29/9/16.
 */
public class Job {
    private String jobName;
    private String busCharterId;
    private String vehicleNo;
    private String serviceType;
    private ArrayList<RoutePath> routePaths;
    private ArrayList<RoutePoint> routePoints;

    public Job(String jobName, String busCharterId, ArrayList<RoutePath> routePaths, ArrayList<RoutePoint> routePoints, String vehicleNo, String serviceType) {
        this.jobName = jobName;
        this.busCharterId = busCharterId;
        this.vehicleNo = vehicleNo;
        this.serviceType = serviceType;
        this.routePaths = routePaths;
        this.routePoints = routePoints;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public ArrayList<RoutePath> getRoutePaths() {
        return routePaths;
    }

    public void setRoutePaths(ArrayList<RoutePath> routePaths) {
        this.routePaths = routePaths;
    }

    public ArrayList<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(ArrayList<RoutePoint> routePoints) {
        this.routePoints = routePoints;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getBusCharterId() {
        return busCharterId;
    }

    public void setBusCharterId(String busCharterId) {
        this.busCharterId = busCharterId;
    }
}

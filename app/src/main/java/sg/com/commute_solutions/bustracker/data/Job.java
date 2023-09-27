package sg.com.commute_solutions.bustracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle on 29/9/16.
 */
public class Job {
    private String jobName;
    private ArrayList<RoutePath> routePaths;
    private ArrayList<RoutePoint> routePoints;

    public Job(String jobName, ArrayList<RoutePath> routePaths, ArrayList<RoutePoint> routePoints) {
        this.jobName = jobName;
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
}

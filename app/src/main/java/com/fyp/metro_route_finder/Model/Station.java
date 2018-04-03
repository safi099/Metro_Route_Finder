package com.fyp.metro_route_finder.Model;

/**
 * Created by SAFI on 3/30/2018.
 */

public class Station {
    String station_id;
    String station_name;
    String station_lat;
    String station_long;
    String distance;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStation_id() {
        return station_id;
    }

    public void setStation_id(String station_id) {
        this.station_id = station_id;
    }

    public String getStation_name() {
        return station_name;
    }

    public void setStation_name(String station_name) {
        this.station_name = station_name;
    }

    public String getStation_lat() {
        return station_lat;
    }

    public void setStation_lat(String station_lat) {
        this.station_lat = station_lat;
    }

    public String getStation_long() {
        return station_long;
    }

    public void setStation_long(String station_long) {
        this.station_long = station_long;
    }


}

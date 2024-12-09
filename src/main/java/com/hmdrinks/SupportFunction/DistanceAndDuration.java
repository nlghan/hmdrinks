package com.hmdrinks.SupportFunction;

public class DistanceAndDuration {
    private double distance; // km
    private String duration; // time in "minute"

    public DistanceAndDuration(double distance, String duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Distance: " + distance + " km, Duration: " + duration;
    }
}

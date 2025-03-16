package com.openclassrooms.tourguide;

import gpsUtil.location.Location;

public class TouristAttractionInformation {

    private String attractionName;

    private Location attractionLocation;

    private Location touristLocation;

    private double distance;

    private int rewardPoints;

    public TouristAttractionInformation(String attractionName, Location attractionLocation, Location touristLocation, double distance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.touristLocation = touristLocation;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public Location getTouristLocation() {
        return touristLocation;
    }

    public void setTouristLocation(Location touristLocation) {
        this.touristLocation = touristLocation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}

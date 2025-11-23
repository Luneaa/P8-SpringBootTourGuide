package com.openclassrooms.tourguide;

import gpsUtil.location.Location;

/**
 * Tourist attraction information model
 */
public class TouristAttractionInformation {

    /**
     * Name of the attraction
     */
    private String attractionName;

    /**
     * Location of the attraction
     */
    private Location attractionLocation;

    /**
     * Location of the tourist
     */
    private Location touristLocation;

    /**
     * Distance between the tourist and the location
     */
    private double distance;

    /**
     * Rewards points awarded for visiting the location
     */
    private int rewardPoints;

    /**
     * Constructor for the tourist attraction information object
     * @param attractionName name of the attraction
     * @param attractionLocation location of the attraction
     * @param touristLocation location of the tourist
     * @param distance distance between the tourist and the attraction
     * @param rewardPoints reward points awarded for visiting the location
     */
    public TouristAttractionInformation(String attractionName, Location attractionLocation, Location touristLocation, double distance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.touristLocation = touristLocation;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    /**
     * Get the attraction name
     * @return attraction name
     */
    public String getAttractionName() {
        return attractionName;
    }

    /**
     * Set the attraction name
     * @param attractionName name of the attraction
     */
    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    /**
     * Get the attraction location
     * @return attraction location
     */
    public Location getAttractionLocation() {
        return attractionLocation;
    }

    /**
     * Set the attraction location
     * @param attractionLocation new attraction location
     */
    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    /**
     * Get the tourist location
     * @return tourist location
     */
    public Location getTouristLocation() {
        return touristLocation;
    }

    /**
     * Set the tourist location
     * @param touristLocation new location of the tourist
     */
    public void setTouristLocation(Location touristLocation) {
        this.touristLocation = touristLocation;
    }

    /**
     * Distance between the tourist and the attraction
     * @return distance in miles
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Set the distance between the tourist and the attraction
     * @param distance new distance in miles
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Get the reward points
     * @return reward points value
     */
    public int getRewardPoints() {
        return rewardPoints;
    }

    /**
     * Set the reward points
     * @param rewardPoints new reward points value
     */
    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}

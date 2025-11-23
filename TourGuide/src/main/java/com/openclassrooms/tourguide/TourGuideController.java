package com.openclassrooms.tourguide;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.openclassrooms.tourguide.service.RewardsService;
import gpsUtil.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

/**
 * Tour guide controller
 */
@RestController
public class TourGuideController {

    /**
     * Tour guide service instance
     */
	@Autowired
	TourGuideService tourGuideService;

    /**
     * Reward service instance
     */
    @Autowired
    RewardsService rewardsService;

    /**
     * Root controller route
     * @return welcome message
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * GetLocation api route
     * @param userName user to get the location from
     * @return async visited location
     */
    @RequestMapping("/getLocation") 
    public CompletableFuture<VisitedLocation> getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }

    /**
     * Returns the nearby attraction for the given username
     * @param userName username to look up
     * @return async list of all the nearby attractions with required informations
     */
    @RequestMapping("/getNearbyAttractions") 
    public CompletableFuture<List<TouristAttractionInformation>> getNearbyAttractions(@RequestParam String userName) {
        var user = getUser(userName);

        // we get the user's visited locations
    	CompletableFuture<VisitedLocation> visitedLocation = tourGuideService.getUserLocation(user);

        // For each location we create an object with the required information
        return visitedLocation.thenApply(userLocation -> tourGuideService.getNearByAttractions(userLocation)
            .stream().map(attraction -> {
                    try {
                        return new TouristAttractionInformation(
                                attraction.attractionName,
                                new Location(attraction.latitude, attraction.longitude),
                                userLocation.location,
                                rewardsService.getDistance(userLocation.location, attraction),
                                rewardsService.getRewardPoints(attraction, user).get()
                        );
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }).toList());
    }

    /**
     * Gets all the rewards for the given user
     * @param userName username of the user to check
     * @return list of all the user's rewards
     */
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }

    /**
     * Gets all the trip deals for a given user
     * @param userName username of the user to check
     * @return list of all the user's trip deals
     */
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }

    /**
     * Get a user based on its username
     * @param userName username to look up
     * @return found user matching the username
     */
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
}
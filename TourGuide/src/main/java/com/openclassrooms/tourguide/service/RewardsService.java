package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

/**
 * Service for user rewards
 */
@Service
public class RewardsService {

	/**
	 * Const to convert miles
	 */
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	/**
	 * Proximity in miles
	 */
    private int defaultProximityBuffer = 10;

	/**
	 * Proximity buffer
	 */
	private int proximityBuffer = defaultProximityBuffer;

	/**
	 * Range from which we consider the user close enough
	 */
	private int attractionProximityRange = 200;

	/**
	 * GpsUtil library, used for GPS operations
	 */
	private final GpsUtil gpsUtil;

	/**
	 * RewardCentral dependency used to compute rewards
	 */
	private final RewardCentral rewardsCentral;

	/**
	 * List of all available attractions
	 */
	private final List<Attraction> attractions;

	/**
	 * Pool of threads to execute some computation
	 * We use 64 threads as it is enough for our usage and can run efficiently en most computers
	 */
	private final ExecutorService executorService = Executors.newFixedThreadPool(64);

	/**
	 * Reward service constructor
	 * @param gpsUtil gps library
	 * @param rewardCentral rewards library
	 */
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
		this.attractions = this.gpsUtil.getAttractions();
	}

	/**
	 * Sets the proximity buffer
	 * @param proximityBuffer value to set
	 */
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	/**
	 * Sets the proximity buffer to its default value
	 */
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Computes the rewards for a user
	 * @param user user to compute the rewards for
	 * @return CompletableFuture allowing to wait for asynchronous operation
	 */
	public CompletableFuture<Void> calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<CompletableFuture<Object>> result = new ArrayList<>();

		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {
				if(nearAttraction(visitedLocation, attraction)) {
					var future = getRewardPoints(attraction, user).thenApply(rewardPoints ->
					{
						user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
						return null;
					});
					result.add(future);
				}
			}
		}

		return CompletableFuture.allOf(result.toArray(new CompletableFuture[0]));
	}

	/**
	 * Indicates if a location is close enough of an attraction
	 * @param attraction attraction to check
	 * @param location location to check
	 * @return bool value, true if close enough
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) <= attractionProximityRange;
	}

	/**
	 * Indicates if a visited location is close enough of an attraction
	 * @param visitedLocation visited location to check
	 * @param attraction attraction to check
	 * @return bool value, true if close enough
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
	}

	/**
	 * Gets the rewards points that the user can get by going to a given attraction
	 * @param attraction attraction to check
	 * @param user user to check
	 * @return Integer value representing the amount of points that the user can be rewarded with
	 */
	public CompletableFuture<Integer> getRewardPoints(Attraction attraction, User user) {
		return CompletableFuture.supplyAsync(() -> {
            return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
        }, executorService);
	}

	/**
	 * Calculate the distance between two locations
	 * @param loc1 first location to check
	 * @param loc2 second location to check
	 * @return distance
	 */
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);

        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}

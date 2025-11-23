package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;

/**
 * Service for tour guide operations
 */
@Service
public class TourGuideService {
	/**
	 * Limit amount of close attraction
	 */
	private static final int CLOSEST_ATTRACTIONS_COUNT = 5;

	/**
	 * Logger for current class
	 */
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);

	/**
	 * GpsUtil library
	 */
	private final GpsUtil gpsUtil;

	/**
	 * RewardsService library
	 */
	private final RewardsService rewardsService;

	/**
	 * Thread pool used for asynchronous operations
	 */
	private final ExecutorService executorService = Executors.newFixedThreadPool(64);

	/**
	 * TripPricer library
	 */
	private final TripPricer tripPricer = new TripPricer();

	/**
	 * List of attractions
	 */
	private final List<Attraction> attractions;

	/**
	 * Object used to track user positions and rewards
	 */
	public final Tracker tracker;

	/**
	 * Toggle test data
	 */
	boolean testMode = true;

	/**
	 * Constructor for tour guide service
	 * @param gpsUtil gps library
	 * @param rewardsService rewards library
	 */
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

		this.attractions = this.gpsUtil.getAttractions();

		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	/**
	 * Gets all the user rewards for a user
	 * @param user user to check
	 * @return list of rewards
	 */
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	/**
	 * Get the user's location
	 * @param user user to check
	 * @return user location
	 */
	public CompletableFuture<VisitedLocation> getUserLocation(User user) {
        if (!user.getVisitedLocations().isEmpty())
		{
			return CompletableFuture.supplyAsync(user::getLastVisitedLocation, executorService);
		}
        else
		{
			return trackUserLocation(user);
		}
	}

	/**
	 * Get a user from its username
	 * @param userName username to check
	 * @return user entity
	 */
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	/**
	 * Get all the users
	 * @return list of all the users
	 */
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	/**
	 * Adds a user to the internal user map
	 * @param user user to add
	 */
	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/**
	 * Gets all the possible trips deals available for a given user, takes into account multiple data
	 * such as the number of adults and children and the trip duration
	 * @param user user to check
	 * @return list of trip deals
	 */
	public List<Provider> getTripDeals(User user) {
		// Get the current sum of rewards
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();

		// Get list of deals for the user
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);

		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * Gets the user's position
	 * @param user user to check
	 * @return Async value for the user's position
	 */
	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		return CompletableFuture.supplyAsync(() -> {
			VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
			user.addToVisitedLocations(visitedLocation);
            try {
                rewardsService.calculateRewards(user).get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return visitedLocation;
		}, executorService);
	}

	/**
     * Returns the five closest attractions from the given location
     */
	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		return gpsUtil.getAttractions().stream()
				.sorted(Comparator.comparingDouble(a -> rewardsService.getDistance(a, visitedLocation.location)))
				.limit(CLOSEST_ATTRACTIONS_COUNT)
				.toList();
	}

	/**
	 * Stops the tracker when the application stops
	 */
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	 /**********************************************************************************
	 *
	 * Methods Below: For Internal Testing
	 *
	 **********************************************************************************/

	/**
	 * API Key used for the tracer library
	 */
	private static final String tripPricerApiKey = "test-server-api-key";

	/**
	 * Database connection will be used for external users, but for testing purposes
	 * internal users are provided and stored in memory
	 */
	private final Map<String, User> internalUserMap = new HashMap<>();

	/**
	 * Initializes the internal users
	 */
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	/**
	 * Generate a random location history
	 * @param user user to generate the history
	 */
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	/**
	 * Generates a random longitude
	 * @return random longitude
	 */
	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/**
	 * Generates a random latitude
	 * @return random latitude
	 */
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/**
	 * Generates a random time close to now (maximum 30 days before now)
	 * @return random time
	 */
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
}

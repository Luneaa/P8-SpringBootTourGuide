package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

/**
 * Tracker utility to track user locations
 */
public class Tracker extends Thread {
	/**
	 * Logger for the tracker class
	 */
	private Logger logger = LoggerFactory.getLogger(Tracker.class);

	/**
	 * Time interval const for polling rate
	 */
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);

	/**
	 * Async thread pool
	 */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	/**
	 * Tour guide service instance
	 */
	private final TourGuideService tourGuideService;

	/**
	 * Boolean used to interrupt the tracker loop
	 */
	private boolean stop = false;

	/**
	 * Constructor for the tracker
	 * @param tourGuideService tour guide service
	 */
	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;

		executorService.submit(this);
	}

	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	/**
	 * Run loop of the tracker
	 */
	@Override
	public void run() {
		// Stopwatch is used to monitor performance
		StopWatch stopWatch = new StopWatch();

		// Infinite loop
		while (true) {
			// Stop condition
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.forEach(u -> tourGuideService.trackUserLocation(u));
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}

	}
}

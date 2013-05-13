package home.poolplayer.controller;

import home.poolplayer.imagecapture.ImageCapture;
import home.poolplayer.imageproc.ImageProcessor;
import home.poolplayer.io.SettingsReader;
import home.poolplayer.messaging.Messages.MessageNames;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.CueStick;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolTable;
import home.poolplayer.model.Robot;
import home.poolplayer.model.Shot;
import home.poolplayer.shotcalculator.ShotCalculator;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Controller extends Thread {

	public static Controller instance;

	private static long WAIT_TIME = 2000;

	private List<PoolBall> balls;
	private CueStick cueStick;
	private PoolTable table;
	private boolean solids;

	private Robot robot;
	
	private ImageCapture imageCapture;
	private ImageProcessor imageProcessor;

	private boolean gameon;

	private Controller() {
		System.loadLibrary("cv2.so");
		System.loadLibrary("libopencv_java245.dylib");

		table = new PoolTable();
		balls = new ArrayList<PoolBall>();
		cueStick = new CueStick();
		solids = true;

		gameon = true;
		imageCapture = ImageCapture.getInstance();
		imageProcessor = ImageProcessor.getInstance();
		
		robot = new Robot();
	}

	public static Controller getInstance() {
		if (instance == null)
			instance = new Controller();
		return instance;
	}

	@Override
	public void run() {
		
		super.run();
	}
	
	
//	@Override
	public void run2() {
		while (gameon) {

			try {
				// Clear all objects
				balls.clear();
				cueStick = null;
				
				
				// Capture image and let UI know
				Mat img = imageCapture.getAvgImageTest();
				Messenger.getInstance().broadcastMessage(
						MessageNames.FRAME_AVAILABLE.name(), img);

				// Find balls and let UI know
				balls = imageProcessor.findBalls(img);
				Messenger.getInstance().broadcastMessage(
						MessageNames.BALLS_DETECTED.name(), balls);

				// Find cueStick and let UI know
				cueStick = imageProcessor.findCueStick(img);
				Messenger.getInstance().broadcastMessage(
						MessageNames.CUESTICK_DETECTED.name(), cueStick);

				Point center = imageProcessor.finRobot(img);
				robot.setCenter(center);
				Messenger.getInstance().broadcastMessage(
						MessageNames.ROBOT_DETECTED.name(), center);
				
				if (center == null) {
					System.out.println("Bot center not found");
					sleep(WAIT_TIME);
					continue;
				}				
				if (balls == null || balls.isEmpty()) {
					System.out.println("No balls found");
					sleep(WAIT_TIME);
					continue;
				}
				if (cueStick == null) {
					System.out.println("No cue stick found");
					sleep(WAIT_TIME);
					continue;
				}

				// Find best shot
				Shot bestShot = ShotCalculator.findBestShot();
				if (bestShot == null) {
					System.out.println("No shot possible");
					sleep(WAIT_TIME);
					continue;
				}
				Messenger.getInstance().broadcastMessage(
						MessageNames.SHOT_FOUND.name(), bestShot);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<PoolBall> getBalls() {
		return balls;
	}

	public PoolTable getTable() {
		return table;
	}

	public CueStick getCueStick() {
		return cueStick;
	}

	public boolean isSolids() {
		return solids;
	}

	public void setSolids(boolean solids) {
		this.solids = solids;
	}
	
	public void setGameon(boolean gameon) {
		this.gameon = gameon;
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public void loadSettings(String settingsFile) {
		if (isAlive())
			interrupt();
		
		new SettingsReader(settingsFile);

		// Init various actors
		table.initPocketPositions();
		boolean success = imageCapture.initialize();
		if (!success) {
			System.out.println("Failed to initialize image capture");
			return;
		}
		
		success = robot.initialize();
		if (!success) {
			System.out.println("Failed to initialize robot");
			return;
		}

		start();
	}
}
package home.poolplayer.controller;

import home.poolplayer.imagecapture.ImageCapture;
import home.poolplayer.imageproc.ImageProcessor;
import home.poolplayer.io.SettingNames;
import home.poolplayer.io.SettingsReader;
import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messages.MessageNames;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.CueStick;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolTable;
import home.poolplayer.model.Robot;
import home.poolplayer.model.Shot;
import home.poolplayer.robot.Move;
import home.poolplayer.robot.PathPlanner;
import home.poolplayer.shotcalculator.ShotCalculator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class Controller extends Thread implements PropertyChangeListener {

	public static Controller instance;
	public static String LOGGERNAME = "PoolPlayer";

	private static long WAIT_TIME = 1000;

	private static Logger logger;

	private List<PoolBall> balls;
	private CueStick cueStick;
	private PoolTable table;
	private boolean solids;

	private Robot robot;

	private ImageCapture imageCapture;
	private ImageProcessor imageProcessor;

	private boolean gameon;
	private boolean pause;

	private boolean uibusy;
	
	private Properties settings;

	private Controller() {
		// System.loadLibrary("cv2.so");
		// System.loadLibrary("libopencv_java245.dylib");

		System.loadLibrary("opencv_java248.dll");

		table = new PoolTable();
		balls = new ArrayList<PoolBall>();
		cueStick = new CueStick();
		solids = true;

		gameon = true;
		pause = false;
		uibusy = false;

		imageCapture = ImageCapture.getInstance();
		imageProcessor = ImageProcessor.getInstance();

		robot = new Robot();

		Messenger.getInstance().addListener(this);

		logger = Logger.getLogger(LOGGERNAME);
		logger.setLevel(Level.DEBUG);
	}

	public static Controller getInstance() {
		if (instance == null)
			instance = new Controller();
		return instance;
	}

	// @Override
	public void run2() {
		while (gameon) {

			try {

				if (pause) {
					sleep(WAIT_TIME);
					continue;
				}

				sendMessageToUIAndWait(MessageNames.CLEARUI, null);

				// Clear all objects
				balls.clear();
				cueStick = null;
//				robot.setCenter(null);

				// Capture image and let UI know
				Mat img = imageCapture.getAvgImage();
				Highgui.imwrite(
						"C:\\Users\\narsi_000\\Documents\\Projects\\images\\calib.png",
						img);
				sendMessageToUIAndWait(MessageNames.FRAME_AVAILABLE, img);
								
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}

	// @Override
	public void run() {
		while (gameon) {

			try {

				if (pause) {
					sleep(WAIT_TIME);
					continue;
				}

				sendMessageToUIAndWait(MessageNames.CLEARUI, null);

				// Clear all objects
				balls.clear();
				cueStick = null;
				robot.setCenter(null);
				System.gc();

				// Capture image and let UI know
				Mat img = imageCapture.getAvgImage();
				sendMessageToUIAndWait(MessageNames.FRAME_AVAILABLE, img);

				// Find balls and let UI know
				balls = imageProcessor.findBalls(img);
				sendMessageToUIAndWait(MessageNames.BALLS_DETECTED, balls);

				// Find cueStick and let UI know
				cueStick = imageProcessor.findCueStick(img);
				sendMessageToUIAndWait(MessageNames.CUESTICK_DETECTED, cueStick);

				if (cueStick == null) {
					sleep(WAIT_TIME);
					continue;
				}
								
				robot.setCenter(cueStick.end);
				logger.info("****** Found robot *******");
				logger.debug(robot.toString());				
				sendMessageToUIAndWait(MessageNames.ROBOT_DETECTED, robot.getCenter());

		 		if (balls == null || balls.isEmpty()) {
					sleep(WAIT_TIME);
					continue;
				}

		 		// Find best shot
				Shot bestShot = ShotCalculator.findBestShot();
				if (bestShot == null) {
					sleep(WAIT_TIME);
					continue;
				}
				sendMessageToUIAndWait(MessageNames.SHOT_FOUND, bestShot);

				List<Move> path = PathPlanner.getPath(bestShot, img);
				sendMessageToUIAndWait(MessageNames.PATH_FOUND, path);

				robot.makeShot(bestShot, path);
				
				Messenger.getInstance().broadcastMessage(MessageNames.PAUSE.name(), true);

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

	public void setCueStick(CueStick cueStick) {
		this.cueStick = cueStick;
	}

	public Properties getSettings() {
		return settings;
	}
	
	public void setSettings(Properties settings) {
		this.settings = settings;
	}
	
	public void loadSettings(String settingsFile) {
		if (isAlive())
			interrupt();

		new SettingsReader(settingsFile);
		String val = settings.getProperty(SettingNames.LOGLEVEL.name());
		if (val != null)
			logger.setLevel(Level.toLevel(val));
		
		// Init various actors
		table.initPocketPositions();
		boolean success = imageCapture.initialize();
		if (!success) {
			logger.fatal("Failed to initialize image capture");
			return;
		}

		success = robot.initialize();
		if (!success) {
			logger.fatal("Failed to initialize robot");
			return;
		}

		start();
	}

	private void sendMessageToUIAndWait(MessageNames message, Object packet) {
		uibusy = true;
		Messenger.getInstance().broadcastMessage(message.name(), packet);
		while (uibusy) {
			try {
				sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (Messages.MessageNames.valueOf(evt.getPropertyName())) {
		case UI_DONE:
			uibusy = false;
			break;
		case PAUSE:
			pause = (Boolean) evt.getNewValue();
			if (pause)
				logger.info("****** System paused *******");
			else
				logger.info("****** System resumed *******");
			break;
		default:
			break;
		}
	}
}
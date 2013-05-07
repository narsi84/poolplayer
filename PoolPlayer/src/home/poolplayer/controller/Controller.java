package home.poolplayer.controller;

import home.poolplayer.imagecapture.ImageCapture;
import home.poolplayer.imageproc.ImageProcessor;
import home.poolplayer.io.SettingsReader;
import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messages.MessageNames;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolTable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

public class Controller implements PropertyChangeListener {

	public static Controller instance;
	
	private List<PoolBall> balls;
	private PoolTable table;
	
	private Controller(){
		System.loadLibrary("cv2.so");
		System.loadLibrary("libopencv_java245.dylib");
		
		Messenger.getInstance().addListener(this);
		table = new PoolTable();
		balls = new ArrayList<PoolBall>();
	}
	
	public static Controller getInstance(){
		if (instance == null)
			instance = new Controller();
		return instance;
	}
	
	public List<PoolBall> getBalls() {
		return balls;
	}
	
	public void setBalls(List<PoolBall> balls) {
		this.balls = balls;
	}
	
	public PoolTable getTable() {
		return table;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch(Messages.MessageNames.valueOf(evt.getPropertyName())){
		case FRAME_AVAILABLE:
			processFrame(evt.getNewValue());
			break;
		default:
			break;
		}
	}

	private void processFrame(Object evtValue){		
		ImageProcessor p = ImageProcessor.getInstance();
		List<PoolBall> ballsDetected = p.findBalls((Mat)evtValue);
		balls = ballsDetected;
		Messenger.getInstance().broadcastMessage(MessageNames.BALLS_DETECTED.name(), balls);
	}
	
	public void loadSettings(String settingsFile) {
		ImageCapture.getInstance().shutDown();
		new SettingsReader(settingsFile);
		
		ImageCapture.getInstance().start();
	}
}
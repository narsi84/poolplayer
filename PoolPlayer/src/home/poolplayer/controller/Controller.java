package home.poolplayer.controller;

import home.poolplayer.imagecapture.ImageCapture;
import home.poolplayer.imageproc.ImageProcessor;
import home.poolplayer.io.SettingsReader;
import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messages.MessageNames;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.PoolBall;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.opencv.core.Mat;

public class Controller implements PropertyChangeListener {

	public static Controller instance;
	
	private Controller(){
		Messenger.getInstance().addListener(this);
		System.loadLibrary("cv2.so");
		System.loadLibrary("libopencv_java245.dylib");
	}
	
	public static Controller getInstance(){
		if (instance == null)
			instance = new Controller();
		return instance;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch(Messages.MessageNames.valueOf(evt.getPropertyName())){
		case START:
			startImageCapture();		
			break;
		case FRAME_AVAILABLE:
			processFrame(evt.getNewValue());
			break;
		default:
			break;
		}
	}

	private void processFrame(Object evtValue){		
		ImageProcessor p = ImageProcessor.getInstance();
		List<PoolBall> balls = p.findBalls((Mat)evtValue);
		Messenger.getInstance().broadcastMessage(MessageNames.BALLS_DETECTED.name(), balls);
	}
	
	private void startImageCapture(){
		ImageCapture imageCapture = ImageCapture.getInstance();
		imageCapture.start();
	}
	
	public void loadSettings(String settingsFile) {
		ImageCapture.getInstance().shutDown();
		new SettingsReader(settingsFile);
		ImageCapture.getInstance().start();
	}
}
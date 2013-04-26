package home.poolplayer.controller;

import home.poolplayer.imagecapture.ImageCapture;
import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
		case FRAME_AVAILABLE:
			break;
		default:
			break;
		}
	}

	private void startImageCapture(){
		ImageCapture imageCapture = ImageCapture.getInstance();
		imageCapture.start();
	}
}

package home.poolplayer.controller;

import home.poolplayer.imagecapture.ImageCapture;
import home.poolplayer.messaging.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Controller implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch(Messages.valueOf(evt.getPropertyName())){
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

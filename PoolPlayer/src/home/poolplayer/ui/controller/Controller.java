package home.poolplayer.ui.controller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Controller {

	private PropertyChangeSupport support;
	
	private static Controller instance;
	
	private Controller() {
		support = new PropertyChangeSupport(this);
	}

	public static Controller getInstance(){
		if (instance == null)
			instance = new Controller();
		return instance;
	}
	
	public void addListener(PropertyChangeListener listener){
		support.addPropertyChangeListener(listener);
	}
	
	public void removeListener(PropertyChangeListener listener){
		support.removePropertyChangeListener(listener);
	}
	
	public void firePropertyChangeEvent(String evt){
		support.firePropertyChange(evt, null, null);
	}
}

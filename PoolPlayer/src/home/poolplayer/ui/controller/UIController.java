package home.poolplayer.ui.controller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class UIController {

	private PropertyChangeSupport support;
	
	private static UIController instance;
	
	private UIController() {
		support = new PropertyChangeSupport(this);
	}

	public static UIController getInstance(){
		if (instance == null)
			instance = new UIController();
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

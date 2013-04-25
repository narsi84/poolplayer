package home.poolplayer.messaging;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Messenger{

	private static Messenger instance;
	
	private PropertyChangeSupport support;
	
	private Messenger(){
		support = new PropertyChangeSupport(this);
	}
	
	public static Messenger getInstance(){
		if (instance == null){
			instance = new Messenger();
		}
		
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
	
	public void firePropertyChangeEvent(String evt, Object newObj){
		support.firePropertyChange(evt, null, newObj);
	}
	
}

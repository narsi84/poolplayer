package home.poolplayer.messaging;

import home.poolplayer.messaging.Messages.MessageNames;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Messenger extends Thread{

	private static Messenger instance;
	
	private PropertyChangeSupport support;
	
	private BlockingQueue<Messages> messageQ;
	private Messenger(){
		support = new PropertyChangeSupport(this);
		messageQ = new ArrayBlockingQueue<Messages>(1000);
	}
	
	public static Messenger getInstance(){
		if (instance == null){
			instance = new Messenger();
			instance.start();
		}
		
		return instance;
	}

	@Override
	public void run() {
		while(true){
			Messages newmsg;
			try {
				newmsg = messageQ.take();
				support.firePropertyChange(newmsg.name.name(), newmsg.oldVal, newmsg.newVal);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void addListener(PropertyChangeListener listener){
		support.addPropertyChangeListener(listener);
	}
	
	public void removeListener(PropertyChangeListener listener){
		support.removePropertyChangeListener(listener);
	}
	
	public void broadcastMessage(String evt){
		Messages m = new Messages();
		m.name = MessageNames.valueOf(evt);
		m.newVal = null;
		m.oldVal = null;
		messageQ.add(m);
	}
	
	public void broadcastMessage(String evt, Object newObj){
		Messages m = new Messages();
		m.name = MessageNames.valueOf(evt);
		m.newVal = newObj;
		m.oldVal = null;
		messageQ.add(m);
	}
}

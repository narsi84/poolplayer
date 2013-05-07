package home.poolplayer.messaging;

public class Messages {

	public enum MessageNames {
		FRAME_AVAILABLE,
		BALLS_DETECTED
	}
	
	public MessageNames name;
	public Object oldVal;
	public Object newVal;
}

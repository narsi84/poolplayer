package home.poolplayer.messaging;

public class Messages {

	public enum MessageNames {
		FRAME_AVAILABLE,
		BALLS_DETECTED,
		CUESTICK_DETECTED,
		SHOT_FOUND,
		ROBOT_DETECTED,
	}
	
	public MessageNames name;
	public Object oldVal;
	public Object newVal;
}

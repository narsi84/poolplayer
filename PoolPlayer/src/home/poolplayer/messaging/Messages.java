package home.poolplayer.messaging;

public class Messages {

	public enum MessageNames {
		FRAME_AVAILABLE,
		BALLS_DETECTED,
		CUESTICK_DETECTED,
		SHOT_FOUND,
		ROBOT_DETECTED,
		PATH_FOUND,
	}
	
	public MessageNames name;
	public Object oldVal;
	public Object newVal;
}

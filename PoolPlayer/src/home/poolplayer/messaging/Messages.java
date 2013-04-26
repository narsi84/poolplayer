package home.poolplayer.messaging;

public class Messages {

	public enum MessageNames {
		START,
		FRAME_AVAILABLE,
	}
	
	public MessageNames name;
	public Object oldVal;
	public Object newVal;
}

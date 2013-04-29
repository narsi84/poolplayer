package home.poolplayer.imagecapture;

import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

/**
 * This class uses OpenCV to capture image from a webcam at a user defined frame
 * rate. It will be the uses of the image thus captured to convert it to
 * required format.
 * 
 * A round robin buffering system is used to store captured images. If
 * processing/rendering is not fast enough, reduce the frame rate.
 * 
 * @author Narsi
 * 
 */

public class ImageCapture extends Thread {

	private static ImageCapture instance;
	private static int BUFFER_SIZE = 100;

	private Mat[] buffer;
	private int writeIndx;
	private int readIndx;
	private int frameRate;

	// Switch on or off
	private boolean capture;
	
	private long sleepTime;

	// ID to tell OpenCV which cam to get images from
	private int deviceId;

	private VideoCapture videoCapture;

	private ImageCapture() {
		frameRate = 10;
		sleepTime = Math.round(1000.0/frameRate);
		writeIndx = 0;
		buffer = new Mat[BUFFER_SIZE];
		deviceId = 0;
		capture = true;
	}

	public static ImageCapture getInstance() {
		if (instance == null)
			instance = new ImageCapture();
		return instance;
	}
	
	@Override
	public void run() {
		videoCapture = new VideoCapture(deviceId);

		//Wait for sometime. Otherwise the mac cam doesnt get initialized.
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (!videoCapture.isOpened()) {
			System.out
					.println("No cam found. Image capture wont be initialized.");
			return;
		}

		while (capture) {
			Mat frame = new Mat();
			boolean succes = videoCapture.read(frame);
			if (!succes){
				//Cam closed or no video
				break;
			}
			
//			System.out.println("Frame captured");
//			System.out.println("Frame stats: " + (int)frame.size().width + "x" + (int)frame.size().height + "x" + frame.channels() + ", " + CvType.typeToString(frame.type()) + ", " + frame.dataAddr());
//			System.out.println(Arrays.toString(frame.get(0, 0)));
			
			buffer[writeIndx++ % BUFFER_SIZE] = frame;
			Messenger.getInstance().broadcastMessage(Messages.MessageNames.FRAME_AVAILABLE.name(), frame);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		videoCapture.release();
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
		sleepTime = Math.round(1000.0/frameRate);
	}
	
	public synchronized Mat getNextFrame(){
		return buffer[readIndx++ % BUFFER_SIZE];
	}
}

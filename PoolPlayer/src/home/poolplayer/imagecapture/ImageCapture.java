package home.poolplayer.imagecapture;

import home.poolplayer.messaging.Messages;
import home.poolplayer.messaging.Messenger;
import home.poolplayer.model.PoolTable;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
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
	private static int BUFFER_SIZE = 10;

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
		sleepTime = Math.round(1000.0 / frameRate);
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


		// Wait for sometime. Otherwise the mac cam doesnt get initialized.
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

		// Fill buffer first to take avg.
		for (int i = 0; i < BUFFER_SIZE; i++) {
			Mat frame = captureTableFrame();
			if (frame == null){
				videoCapture.release();
				return;
			}
			
			buffer[i] = frame;			
		}

		while (capture) {

			Mat frame = captureTableFrame();
			if (frame == null){
				break;
			}
			
			writeIndx = ++writeIndx % BUFFER_SIZE;
			buffer[writeIndx] = frame;

			Mat avgImg = getAvgImage();
			Messenger.getInstance().broadcastMessage(
					Messages.MessageNames.FRAME_AVAILABLE.name(), avgImg);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		videoCapture.release();
	}

	private Mat captureTableFrame(){
		Mat frame = new Mat();
		boolean succes = videoCapture.read(frame);
		if (!succes) {
			System.out.println("No cam");
			videoCapture.release();
			return null;
		}			
		
		PoolTable t = PoolTable.getInstance();
		Rect roi = new  Rect(frame.width()/2 - t.getWidth()/2, frame.height()/2 - t.getHeight(), t.getWidth(), t.getHeight());
		Mat tframe = frame.submat(roi);
		Mat clone = tframe.clone();
		clone.convertTo(clone, CvType.CV_16UC3);
		
		return clone;		
	}
	
	private Mat getAvgImage() {
		Mat avgImage = buffer[0].clone();
		for (int i = 1; i < BUFFER_SIZE; i++) {
			Core.add(avgImage, buffer[i], avgImage);
		}
		Core.multiply(avgImage, new Scalar(1.0/BUFFER_SIZE, 1.0/BUFFER_SIZE, 1.0/BUFFER_SIZE), avgImage);
		
		avgImage.convertTo(avgImage, CvType.CV_8UC3);
		return avgImage;
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
		sleepTime = Math.round(1000.0 / frameRate);
	}

	public synchronized Mat getNextFrame() {
		return buffer[readIndx++ % BUFFER_SIZE];
	}
	
	public void shutDown(){
		capture = false;
		instance = null;
		
		if (videoCapture != null)
			videoCapture.release();
	}
	
}

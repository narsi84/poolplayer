package home.poolplayer.imagecapture;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public class ImageCapture {

	private static ImageCapture instance;
//	private static int BUFFER_SIZE = 100;
	private static int BUFFER_SIZE = 20;

	private static int FRAME_INDEX;

	// ID to tell OpenCV which cam to get images from
	private int deviceId;

	private VideoCapture videoCapture;

	private ImageCapture() {
		deviceId = 0;
	}

	public static ImageCapture getInstance() {
		if (instance == null)
			instance = new ImageCapture();
		return instance;
	}

	public boolean initialize() {
		videoCapture = new VideoCapture(deviceId);

		// Wait for sometime. Otherwise the mac cam doesnt get initialized.
		try {
			Thread.sleep(4000);
			videoCapture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 2000);
			videoCapture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 4000);

			System.out.println("Height: "
					+ videoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
			System.out.println("Width: "
					+ videoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH));

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (!videoCapture.isOpened()) {
			System.out
					.println("No cam found. Image capture wont be initialized.");
			return false;
		}
		return true;
	}

	public Mat getAvgImage() {
		Mat avgImage = capture();

		for (int i = 1; i < BUFFER_SIZE; i++) {
			Mat frame = capture();
			Core.add(avgImage, frame, avgImage);
		}
		Core.multiply(avgImage, new Scalar(1.0 / BUFFER_SIZE,
				1.0 / BUFFER_SIZE, 1.0 / BUFFER_SIZE), avgImage);

		avgImage.convertTo(avgImage, CvType.CV_8UC3);

		return avgImage;
	}

//	public Mat getAvgImageTest() {
//		return TestHough.doProc();
//	}

	public Mat getAvgImageTest() {
		Mat avgImage = captureTableFrameTest();

		for (int i = 1; i < BUFFER_SIZE; i++) {
			Mat frame = captureTableFrameTest();
			Core.add(avgImage, frame, avgImage);
		}
		Core.multiply(avgImage, new Scalar(1.0 / BUFFER_SIZE,
				1.0 / BUFFER_SIZE, 1.0 / BUFFER_SIZE), avgImage);

		avgImage.convertTo(avgImage, CvType.CV_8UC3);

		return avgImage;
	}

	private Mat captureTableFrameTest() {
		FRAME_INDEX = (++FRAME_INDEX % 390);
		int indx = FRAME_INDEX + 1;
		Mat frame = Highgui
				.imread("/Users/narsir/Documents/Projects/Poolplayer/images/test/test"
						+ String.format("%03d", indx) + ".png");

		Mat clone = frame.clone();
		clone.convertTo(clone, CvType.CV_16UC3);

		return clone;
	}

	private Mat capture() {
		Mat frame = new Mat();
		boolean succes = videoCapture.read(frame);
		if (!succes) {
			System.out.println("No cam");
			videoCapture.release();
			return null;
		}

		Mat clone = frame.clone();
		clone.convertTo(clone, CvType.CV_16UC3);

		return clone;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
}

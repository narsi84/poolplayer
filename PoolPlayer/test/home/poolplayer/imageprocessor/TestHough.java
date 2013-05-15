package home.poolplayer.imageprocessor;

import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class TestHough {

	public static void main(String[] args) {
		
		String.format("%03d", 2);
		String.format("%03d", 20);
		
		System.out.println(System.getProperty("java.library.path"));

		System.loadLibrary("cv2.so");
		System.loadLibrary("libopencv_java245.dylib");

		doProc();		
	}
	
	public static Mat doProc() {
		Mat src = Highgui
				.imread("/Users/narsir/Documents/Projects/Poolplayer/images/rgb.png");

		Mat mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(1));
		Core.rectangle(mask, new Point(160, 575), new Point(620, 850), new Scalar(0), -1);

		Mat avgImage = new Mat();
		src.copyTo(avgImage, mask);
		avgImage.convertTo(avgImage, CvType.CV_16UC3);

//		Mat avgImage = src;
		
		Mat img = new Mat();
		avgImage.convertTo(img, CvType.CV_8UC3);

		Mat hsv = new Mat();
		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV_FULL);
		
		Mat redPixels1 = new Mat();
		Core.inRange(hsv, new Scalar(150, 0, 0), new Scalar(255, 255, 15), redPixels1);
		
		Mat redPixels2 = new Mat();
		Core.inRange(hsv, new Scalar(150, 0, 225), new Scalar(255, 255, 255), redPixels2);
		
		Mat stickPixels = new Mat();
		Core.bitwise_or(redPixels1, redPixels2, stickPixels);
        
		Highgui.imwrite("/Users/narsir/Documents/Projects/Poolplayer/images/stickPixels.png", stickPixels);
		
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(stickPixels, stickPixels, Imgproc.MORPH_CLOSE, kernel);
    
		Mat lines = new Mat();
		Imgproc.HoughLinesP(stickPixels, lines, 1, Math.PI / 180, 150, 100, 40);

		if (lines.empty())
			return avgImage;
		
		double[] line = lines.get(0, 0);
		
		Point start = new Point(line[0], line[1]);
		Point end = new Point(line[2], line[3]);
		double angle = Math.atan2(line[3] - line[1] , line[2] - line[0]);

		for (int i = 0; i < lines.width(); i++) {
			line = lines.get(0, i);
			
			System.out.println(Arrays.toString(line));
			
			double thisangle = Math.atan2(line[3] - line[1] , line[2] - line[0]);
			if (Math.abs(thisangle - angle) < 0.02){
			
				if (line[0] < start.x){
					start.x = line[0];
					start.y = line[1];
				}

				if (line[2] > end.x){
					end.x = line[2];
					end.y = line[3];
				}				
			}			
		}		

		Mat circles = new Mat();
				
		Imgproc.HoughCircles(stickPixels, circles,
				Imgproc.CV_HOUGH_GRADIENT, 1, 10, 350, 10, 10, 20);

		
		double[] maxCircle = circles.get(0, 0);
		for(int i=0; i<circles.width(); i++){
			double[] circle = circles.get(0, i);
			System.out.println(Arrays.toString(circle));
			if (circle[2] > maxCircle[2])
				maxCircle = circle;
		}

		if (maxCircle != null)
			Core.circle(avgImage, new Point(maxCircle[0], maxCircle[1]), (int)maxCircle[2], new Scalar(255, 0, 0), 2);
		
		Core.line(avgImage, start, end, new Scalar(255, 0, 0), 2);
		
		Highgui.imwrite("/Users/narsir/Documents/Projects/Poolplayer/images/testimg_out.png", avgImage);
		return avgImage;
	}
		
}

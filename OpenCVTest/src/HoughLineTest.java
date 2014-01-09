import java.util.Arrays;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class HoughLineTest extends Application {
	private static int[] table  =
		//0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1
		 {0,0,0,0,0,0,1,3,0,0,3,1,1,0,1,3,0,0,0,0,0,0,0,0,0,0,2,0,3,0,3,3,
		  0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,3,0,2,2,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,3,0,0,0,0,0,0,0,3,0,0,0,3,0,2,0,
		  0,0,3,1,0,0,1,3,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
		  3,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,3,1,3,0,0,1,3,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,3,0,1,0,0,0,1,0,0,0,0,0,0,0,0,3,3,0,1,0,0,0,0,2,2,0,0,2,0,0,0};
		  
	private static int[] table2  =
		  //0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1
		 {0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,2,2,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,2,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,
		  0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	public Mat doProc() {
		Mat src = Highgui
				.imread("/Users/narsir/Documents/Projects/Poolplayer/images/testimg.png");

		Mat mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(1));
		Core.rectangle(mask, new Point(160, 575), new Point(620, 850), new Scalar(0), -1);

		Mat avgImage = new Mat();
		src.copyTo(avgImage, mask);
		avgImage.convertTo(avgImage, CvType.CV_16UC3);

//		for (int i = 101; i < 200; i++) {
//			try {
//				Mat frame = Highgui
//						.imread("/Users/narsir/Documents/Projects/Poolplayer/images/cuestick/stick0"
//								+ i + ".png");
//				Mat clone = frame.clone();
//				clone.convertTo(clone, CvType.CV_16UC3);
//				Core.add(avgImage, clone, avgImage);
//			} catch (Exception e) {
//				System.out.println(i);
//			}
//		}
//
//		Core.multiply(avgImage, new Scalar(1.0 / 100, 1.0 / 100, 1.0 / 100),
//				avgImage);

		Mat img = new Mat();
		avgImage.convertTo(img, CvType.CV_8UC3);

		Mat hsv = new Mat();
		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV_FULL);
		
		Mat redPixels1 = new Mat();
		Core.inRange(hsv, new Scalar(0, 0, 150), new Scalar(30, 255, 255), redPixels1);
		
		Mat redPixels2 = new Mat();
		Core.inRange(hsv, new Scalar(225, 0, 150), new Scalar(255, 255, 255), redPixels2);
		
		Mat stickPixels = new Mat();
		Core.bitwise_or(redPixels1, redPixels2, stickPixels);
        
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

		//		double avgAngle = angle;
//		int ctr = 1;
		
		for (int i = 0; i < lines.width(); i++) {
			line = lines.get(0, i);
			
			System.out.println(Arrays.toString(line));
			
			double thisangle = Math.atan2(line[3] - line[1] , line[2] - line[0]);
			if (Math.abs(thisangle - angle) < 0.02){
//				avgAngle += angle;
//				ctr++;
				
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
//		avgAngle /= ctr;
//		double len = Math.sqrt( (start.y - end.y)*(start.y - end.y) + (start.x - end.x)*(start.x - end.x) );
//		start.x = - len * Math.cos(avgAngle) + end.x;
//		start.y = - len * Math.sin(avgAngle) + end.y;
		
		Mat circles = new Mat();
				
		Imgproc.HoughCircles(stickPixels, circles,
				Imgproc.CV_HOUGH_GRADIENT, 1, 10, 350, 10, 20, 40);

		
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
		return avgImage;
	}
		
//	private Mat doProc2(){				
//		Mat lines = new Mat();
////		Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 150, 100, 40);
////
////		if (lines.empty())
////			return edges;
////		
//		double[] line = lines.get(0, 0);
//		
//		Point start = new Point(line[0], line[1]);
//		Point end = new Point(line[2], line[3]);
//		double angle = Math.atan2(line[3] - line[1] , line[2] - line[0]);
//		
//		for (int i = 0; i < lines.width(); i++) {
//			line = lines.get(0, i);
//			
//			System.out.println(Arrays.toString(line));
//			
//			double thisangle = Math.atan2(line[3] - line[1] , line[2] - line[0]);
//			if (Math.abs(thisangle - angle) < 0.02){
//				if (line[0] < start.x){
//					start.x = line[0];
//					start.y = line[1];
//				}
//
//				if (line[2] > end.x){
//					end.x = line[2];
//					end.y = line[3];
//				}				
//			}
//			
//		}
//		
//		System.out.println(start.toString() + ", " + end.toString());
//		
////		Core.line(edges, start, end, new Scalar(255), 2);
////		return edges;
//	}
	
	private Mat doProc3(){
		Mat src = Highgui.imread("/Users/narsir/Documents/Projects/Poolplayer/images/stickpixels.png");
		Mat stickPixels = new Mat();
		
		Imgproc.cvtColor(src, stickPixels, Imgproc.COLOR_BGR2GRAY);
//		src.convertTo(stickPixels, CvType.CV_8UC1);
		
//		Mat stickPixels = new Mat(100, 100, CvType.CV_8UC1, new Scalar(0));
//		Core.circle(stickPixels, new Point(50, 50), 20, new Scalar(255), -1);
		
		Mat circles = new Mat();
		Imgproc.HoughCircles(stickPixels, circles,
				Imgproc.CV_HOUGH_GRADIENT, 1, 10, 100, 10, 20, 50);
	
		for(int i=0; i<circles.width(); i++){
			double[] circle = circles.get(0, i);
			System.out.println(Arrays.toString(circle));
			Core.circle(src, new Point(circle[0], circle[1]), (int)circle[2], new Scalar(255, 0, 0), 2);
		}
		
		return src;
	}
	
	private Mat skeletonize2(Mat src){
		Mat skel = new Mat(src.size(), CvType.CV_8UC1, new Scalar(0));
		Mat temp = new Mat();
		Mat eroded = new Mat();
		 
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
		 
		boolean done;		
		do
		{
		  Imgproc.erode(src, eroded, element);
		  Imgproc.dilate(eroded, temp, element); // temp = open(img)
		  Core.subtract(src, temp, temp);
		  Core.bitwise_or(skel, temp, skel);
		  eroded.copyTo(src);
		 
		  done = (Core.countNonZero(src) == 0);
		} while (!done);	
		
		return skel;
	}
	
	private Mat skeletonize(Mat src){		
		int pass = 0;
		int pixelsRemoved;

		Mat mat = new Mat();
		src.copyTo(mat);
		do {
			pixelsRemoved = thin(pass++, table, mat);
			pixelsRemoved += thin(pass++, table, mat);
		} while (pixelsRemoved>0);
		do { // use a second table to remove "stuck" pixels
			pixelsRemoved = thin(pass++, table2, mat);
			pixelsRemoved += thin(pass++, table2, mat);
		} while (pixelsRemoved>0);
		
		return mat;		
	}

	public void start(Stage stage) {
		Mat mat = doProc();

		Group root = new Group();
		Scene scene = new Scene(root, mat.width(), mat.height());

		Image edgeImage = Utils.Mat2Image(mat);
		ImageView imgView = new ImageView(edgeImage);
		root.getChildren().add(imgView);

		stage.setScene(scene);
		stage.setTitle("Gray");
		stage.show();
	}
	
	int thin(int pass, int[] table, Mat src) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int bgColor = 0; //255
			
		int v, index, code;
        int pixelsRemoved = 0;
        
		Mat mat = new Mat();
		src.copyTo(mat);

		for (int y=1; y<mat.width()-1; y++) {
			for (int x=1; x<mat.height()-1; x++) {
				double[] vals = mat.get(x, y);
				p5 = (int)vals[0];
				v = p5;
				if (v!=bgColor) {
					vals = mat.get(x-1, y-1);
					p1 = (int)vals[0];
					
					vals = mat.get(x-1, y);
					p2 = (int)vals[0];

					vals = mat.get(x-1, y+1);
					p3 = (int)vals[0];

					vals = mat.get(x-1, y);
					p4 = (int)vals[0];

					vals = mat.get(x+1, y);
					p6 = (int)vals[0];

					vals = mat.get(x-1, y+1);
					p7 = (int)vals[0];

					vals = mat.get(x, y+1);
					p8 = (int)vals[0];

					vals = mat.get(x+1, y+1);
					p9 = (int)vals[0];

					index = 0;
					if (p1!=bgColor) index |= 1;
					if (p2!=bgColor) index |= 2;
					if (p3!=bgColor) index |= 4;
					if (p6!=bgColor) index |= 8;
					if (p9!=bgColor) index |= 16;
					if (p8!=bgColor) index |= 32;
					if (p7!=bgColor) index |= 64;
					if (p4!=bgColor) index |= 128;
					code = table[index];
					if ((pass&1)==1) { //odd pass
						if (code==2||code==3) {
							v = bgColor;
							pixelsRemoved++;
						}
					} else { //even pass
						if (code==1||code==3) {
							v = bgColor;
							pixelsRemoved++;
						}
					}
				}
				src.put(x, y, v);
			}
		}
		return pixelsRemoved;
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Application.launch(args);
	}
}

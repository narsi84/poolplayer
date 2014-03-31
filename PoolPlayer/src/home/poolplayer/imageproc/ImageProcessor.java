package home.poolplayer.imageproc;

import home.poolplayer.controller.Controller;
import home.poolplayer.model.BallType;
import home.poolplayer.model.CueStick;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolCircle;
import home.poolplayer.model.PoolTable;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageProcessor {

	private static int NUM_CHANNELS = 3;
	private static ImageProcessor instance;
	private static Logger logger;

	private List<PoolBall> balls;
	private AnalysisParams params;

	private Mat tableImg;

	public static ImageProcessor getInstance() {
		if (instance == null)
			instance = new ImageProcessor();
		return instance;
	}

	private ImageProcessor() {
		balls = new ArrayList<PoolBall>();
		params = new AnalysisParams();

		logger = Logger.getLogger(Controller.LOGGERNAME);
	}

	public List<PoolBall> findBalls(Mat img) {
		logger.info("****** Finding balls *******");

		PoolTable t = Controller.getInstance().getTable();
		Rect roi = new Rect(t.getX(), t.getY(), t.getWidth(), t.getHeight());
		tableImg = img.submat(roi);

		this.balls.clear();

		findHoughCircles();

		removeNoisyBalls();
		removeBallsInPockets();
		findBallType();
		findCueBall();

		// Set global coordinates
		for (PoolBall ball : balls) {
			ball.setX(ball.getX() + t.getX());
			ball.setY(ball.getY() + t.getY());
		}

		findAvgBallSize();

		if (balls.isEmpty())
			logger.info("****** No balls found *******");
		else
			logger.info("****** Balls found *******");
		
		for (PoolBall ball : balls) {
			logger.debug(ball.toString());
		}

		return balls;
	}

	/**
	 * Convert image to HSV and find all red bright pixels. Remove noise by
	 * morph closing. Find Hough lines. Find the longest line from a set of
	 * segments.
	 * 
	 * @param img
	 * @return
	 */
	public CueStick findCueStick(Mat img) {
		logger.info("****** Finding cue stick *******");

		if (logger.getLevel().toInt() >= Level.DEBUG.toInt()) {
			Mat dmp = new Mat();
			Imgproc.cvtColor(img, dmp, Imgproc.COLOR_BGR2RGB);
			Highgui.imwrite(
					"C:\\Users\\narsi_000\\Documents\\Projects\\images\\rgb.png",
					dmp);
		}
		
		
		Mat hsv = new Mat();
		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV_FULL);

		// In HSV, red pixels are H(225-15) and V(150-255)
		Mat redPixels1 = new Mat();
		Core.inRange(hsv, new Scalar(0, 0, 150), new Scalar(15, 255, 255),
				redPixels1);

		Mat redPixels2 = new Mat();
		Core.inRange(hsv, new Scalar(225, 0, 150), new Scalar(255, 255, 255),
				redPixels2);

		Mat stickPixels = new Mat();
		Core.bitwise_or(redPixels1, redPixels2, stickPixels);

		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(5, 5));
		Imgproc.morphologyEx(stickPixels, stickPixels, Imgproc.MORPH_CLOSE,
				kernel);

		if (logger.getLevel().toInt() >= Level.DEBUG.toInt()) {
			Highgui.imwrite(
					"C:\\Users\\narsi_000\\Documents\\Projects\\images\\stickpixels.png",
					stickPixels);
		}
		
		Mat lines = new Mat();
		Imgproc.HoughLinesP(stickPixels, lines, 1, Math.PI / 180,
				(int) params.getBlackLevel(), 100, 40);

		if (lines.empty()){
			logger.info("****** No cue stick found *******");
			return null;
		}

		double[] line = lines.get(0, 0);

		Point start = new Point(line[0], line[1]);
		Point end = new Point(line[2], line[3]);
		double angle = Math.atan2(line[3] - line[1], line[2] - line[0]);

		for (int i = 0; i < lines.width(); i++) {
			line = lines.get(0, i);

			double thisangle = Math.atan2(line[3] - line[1], line[2] - line[0]);
			if (Math.abs(thisangle - angle) < 0.02) {
				if (line[0] < start.x) {
					start.x = line[0];
					start.y = line[1];
				}

				if (line[2] > end.x) {
					end.x = line[2];
					end.y = line[3];
				}
			}
		}

		CueStick stick = new CueStick(start, end);
		
		logger.info("****** Found cue stick *******");
		logger.debug(stick.toString());
		
		return stick;
	}

	public Point finRobot(Mat src) {
		logger.info("****** Finding robot *******");
		
		// Mask out the table
		PoolTable t = Controller.getInstance().getTable();
		Mat mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(1));
		Core.rectangle(mask, new Point(t.getX()- t.getPocketRadius(), t.getY() - t.getPocketRadius()), new Point(t.getX()
				+ t.getWidth() + t.getPocketRadius(), t.getY() + t.getHeight() + t.getPocketRadius()), new Scalar(0), -1);

		Mat img = new Mat();
		src.copyTo(img, mask);

		Mat hsv = new Mat();
		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV_FULL);

		// In HSV, red pixels are H(225-15) and V(150-255)
		Mat redPixels1 = new Mat();
		Core.inRange(hsv, new Scalar(0, 0, 150), new Scalar(15, 255, 255),
				redPixels1);

		Mat redPixels2 = new Mat();
		Core.inRange(hsv, new Scalar(225, 0, 150), new Scalar(255, 255, 255),
				redPixels2);

		Mat stickPixels = new Mat();
		Core.bitwise_or(redPixels1, redPixels2, stickPixels);

		Mat circles = new Mat();

		Imgproc.HoughCircles(stickPixels, circles, Imgproc.CV_HOUGH_GRADIENT,
				1, 10, params.getHoughThreshold(), params.getAccumulatorThreshold()/2, params.getMinRadius(), params.getMaxRadius()*2);

		// Find the circle that has the most area
		double[] c = circles.get(0, 0); 
		if (c == null){
			logger.info("****** No robot found *******");
			return null;
		}

		mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(0));
		Point maxCircle = new Point(c[0], c[1]);
		Core.circle(mask, maxCircle, (int)c[2], new Scalar(1), -1);
		Mat circle = new Mat();
		stickPixels.copyTo(circle, mask);
		int maxCircleArea = Core.countNonZero(circle);
				
		for (int i = 1; i < circles.width(); i++) {						
			c = circles.get(0, i);
			
			mask = new Mat(src.size(), CvType.CV_8UC1, new Scalar(0));
			Point center = new Point(c[0], c[1]);
			Core.circle(mask, center, (int)c[2], new Scalar(1), -1);
			circle = new Mat();
			stickPixels.copyTo(circle, mask);
			int circleArea = Core.countNonZero(circle);
			
			if (circleArea > maxCircleArea)
				maxCircle = center;			
		}

		logger.info("****** Found robot *******");
		logger.debug(maxCircle.toString());
		
		return maxCircle;
	}

	private void findHoughCircles() {
		List<Mat> rgb = new ArrayList<Mat>();
		Core.split(tableImg, rgb);

		Mat circles = new Mat();

		for (int n = 0; n < NUM_CHANNELS; n++) {
			Mat channel = rgb.get(n);

			Mat smooth = new Mat();
			Imgproc.GaussianBlur(channel, smooth, new Size(5.0, 5.0), 0.5);

			Imgproc.HoughCircles(smooth, circles, Imgproc.CV_HOUGH_GRADIENT, 1,
					params.getMinCircleDist(), params.getHoughThreshold(),
					params.getAccumulatorThreshold(), params.getMinRadius(),
					params.getMaxRadius());

			for (int i = 0; i < circles.cols(); i++) {
				double[] circ = circles.get(0, i);
				if (circ != null)
					balls.add(new PoolBall(circ));
			}
		}
	}

	private void removeNoisyBalls() {
		List<PoolBall> toRemove = new ArrayList<PoolBall>();
		for (int i = 0; i < balls.size(); i++) {
			for (int j = i + 1; j < balls.size(); j++) {
				PoolBall c1 = balls.get(i);
				PoolBall c2 = balls.get(j);
				if (c1 == c2)
					continue;
				double dist = Math.sqrt((c1.getX() - c2.getX())
						* (c1.getX() - c2.getX()) + (c1.getY() - c2.getY())
						* (c1.getY() - c2.getY()));
				if (dist < params.getMinCircleDist()) {
					if (c1.getR() < c2.getR())
						toRemove.add(c1);
					else
						toRemove.add(c2);
				}
			}
		}

		balls.removeAll(toRemove);
	}

	private void removeBallsInPockets() {
		List<PoolBall> toRemove = new ArrayList<PoolBall>();
		PoolTable t = Controller.getInstance().getTable();
		for (PoolBall ball : balls) {
			for (PoolCircle pocket : t.getPockets()) {
				if (pocket.isPointWithin(ball.getX() + t.getX(), ball.getY() + t.getY()))
					toRemove.add(ball);
			}
		}
		balls.removeAll(toRemove);
	}

	private void findBallType() {
		for (PoolBall c : balls) {
			double totPix = 0;
			double numWhite = 0;
			double numBlack = 0;
			for (int i = (int) (c.getX() - c.getR()); i <= (int) (c.getX() + c
					.getR()); i++) {
				for (int j = (int) (c.getY() - c.getR()); j <= (int) (c.getY() + c
						.getR()); j++) {
					if (!(Math.sqrt((i - c.getX()) * (i - c.getX())
							+ (j - c.getY()) * (j - c.getY())) <= c.getR()))
						continue;

					if (i < 0 || i >= tableImg.width() || j < 0
							|| j >= tableImg.height())
						continue;

					totPix++;

					double[] pixVals = tableImg.get(j, i);
					if (pixVals == null)
						System.out.println(i + ", " + j);
					double maxval = pixVals[0] > pixVals[1] ? pixVals[0]
							: pixVals[1];
					maxval = maxval > pixVals[2] ? maxval : pixVals[2];
					if (pixVals[0] / maxval + pixVals[1] / maxval + pixVals[2]
							/ maxval > params.getColorRatioThreshold()) {
						if (maxval < params.getBlackLevel())
							numBlack++;
						else
							numWhite++;
					}

				}
			}

			if (numWhite / totPix < params.getWhitePixelRatio()) {
				if (numBlack / totPix > params.getBlackBallPixelRatio())
					c.setType(BallType.BLACK);
				else
					c.setType(BallType.SOLID);
			} else {
				if (numWhite / totPix > params.getCueBallPixelRatio())
					c.setType(BallType.CUE);
				else
					c.setType(BallType.STRIPE);
			}
		}
	}

	private void findCueBall() {
		List<PoolBall> cueballs = new ArrayList<PoolBall>();
		PoolBall biggestCue = null;
		for (PoolBall hc : balls) {
			if (hc.getType() == BallType.CUE) {
				cueballs.add(hc);
				if (biggestCue == null)
					biggestCue = hc;
				else if (hc.getR() > biggestCue.getR())
					biggestCue = hc;
			}
		}

		if (cueballs.size() > 1) {
			for (PoolBall hc : cueballs) {
				if (hc != biggestCue)
					hc.setType(BallType.SOLID);
			}
		}
	}

	private void findAvgBallSize() {
		double r = 0;
		for (PoolBall ball : balls) {
			r += ball.getR();
		}
		if (balls.size() > 0)
			PoolBall.AVG_SIZE = r / balls.size();
	}

	public AnalysisParams getParams() {
		return params;
	}
}
package home.poolplayer.imageproc;

import home.poolplayer.controller.Controller;
import home.poolplayer.model.BallType;
import home.poolplayer.model.CueStick;
import home.poolplayer.model.PoolBall;
import home.poolplayer.model.PoolCircle;
import home.poolplayer.model.PoolTable;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageProcessor {

	private static int NUM_CHANNELS = 3;
	private static ImageProcessor instance;
	
	private List<PoolBall> balls;
	private AnalysisParams params;

	private Mat tableImg;
	
	public static ImageProcessor getInstance(){
		if (instance == null)
			instance = new ImageProcessor();
		return instance;
	}
	
	private ImageProcessor() {
		balls = new ArrayList<PoolBall>();
		params = new AnalysisParams();
	}

	public List<PoolBall> findBalls(Mat img) {
		PoolTable t = Controller.getInstance().getTable();
		Rect roi = new  Rect(t.getX(), t.getY(), t.getWidth(), t.getHeight());
		tableImg = img.submat(roi);
	
		this.balls.clear();

		findHoughCircles();
		removeNoisyBalls();
		removeBallsInPockets();
		findBallType();
		findCueBall();
		
		// Set global coordinates
		for (PoolBall ball : balls){
			ball.setX(ball.getX() + t.getX());
			ball.setY(ball.getY() + t.getY());
		}
		
		findAvgBallSize();

		return balls;
	}
	
	public CueStick findCueStick(Mat img){
		List<Mat> rgb = new ArrayList<Mat>();
		Core.split(img, rgb);

		Mat minChannel = new Mat(img.size(), CvType.CV_8UC1, new Scalar(255));
		for(Mat channel : rgb)
			Core.min(channel, minChannel, minChannel);
		
		Mat redChannel = new Mat();
		Core.subtract(rgb.get(2), minChannel, redChannel);

		Mat smooth = new Mat();
		Imgproc.GaussianBlur(redChannel, smooth, new Size(5.0, 5.0), 0.5);

		Mat edges = new Mat();
		Imgproc.Canny(redChannel, edges, params.getHoughThreshold()/2, params.getHoughThreshold());

		Mat lines = new Mat();
		Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 40, 40, 20);
		
		double[] line = lines.get(0, 0);
		CueStick cueStick = new CueStick();
		cueStick.start = new Point(line[0], line[1]);
		cueStick.end = new Point(line[2], line[3]);
		                                                     
		return cueStick;
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
				if (dist < 5){
					if (c1.getR() < c2.getR())
						toRemove.add(c1);
					else
						toRemove.add(c2);
				}
			}
		}

		balls.removeAll(toRemove);
	}

	private void removeBallsInPockets(){
		List<PoolBall> toRemove = new ArrayList<PoolBall>();
		for(PoolBall ball : balls){
			for(PoolCircle pocket : Controller.getInstance().getTable().getPockets()){
				if (pocket.isPointWithin(ball.getX(), ball.getY()))
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

	private void findAvgBallSize(){
		double r = 0;
		for(PoolBall ball : balls){
			r += ball.getR();
		}
		if (balls.size() > 0)
			PoolBall.AVG_SIZE = r/balls.size();
	}
	
	public AnalysisParams getParams() {
		return params;
	}
}
package home.poolplayer.imageproc;

import home.poolplayer.model.BallType;
import home.poolplayer.model.PoolBall;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageProcessor {

	private static int NUM_CHANNELS = 3;
	private static ImageProcessor instance;
	
	private List<PoolBall> balls;
	private AnalysisParams params;

	private Mat table;
	
	public static ImageProcessor getInstance(){
		if (instance == null)
			instance = new ImageProcessor();
		return instance;
	}
	
	private ImageProcessor() {
		balls = new ArrayList<PoolBall>();
		params = new AnalysisParams();
	}

	public List<PoolBall> findBalls(Mat table) {
		this.table = table;

		this.balls.clear();

		doHoughTx();
		removeNoisyBalls();
		findBallType();
		findCueBall();
		return balls;
	}

	private void doHoughTx() {
		List<Mat> rgb = new ArrayList<Mat>();
		Core.split(table, rgb);

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
				if (dist < 5)
					toRemove.add(c1);
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

					if (i < 0 || i >= table.width() || j < 0
							|| j >= table.height())
						continue;

					totPix++;

					double[] pixVals = table.get(j, i);
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

	public Mat getTable() {
		return table;
	}

	public AnalysisParams getParams() {
		return params;
	}

	public void setParams(AnalysisParams params) {
		this.params = params;
	}
}

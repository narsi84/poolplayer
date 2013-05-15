package home.poolplayer.imageprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Test {

	enum BallType {
		CUE, SOLID, STRIPE, BLACK
	}

	static class HoughCirle {
		double x;
		double y;
		double r;
		BallType type;

		public HoughCirle() {
		}

		public HoughCirle(double[] circle) {
			x = circle[0];
			y = circle[1];
			r = circle[2];
		}
	}

	public static Mat doProc() {
		// load the image

		try {
			Mat img = Highgui
					.imread("/Users/narsir/Documents/Projects/Poolplayer/images/stickPixels.png");

			Rect roi = new Rect(160, 575, 460, 275);

			Mat src = new Mat();
			src = img.submat(roi);

			List<Mat> rgb = new ArrayList<Mat>();
			Core.split(src, rgb);

			Mat circles = new Mat();
			List<HoughCirle> houghCircles = new ArrayList<Test.HoughCirle>();

			for (int n = 0; n < 3; n++) {
				Mat channel = rgb.get(n);

				Mat smooth = new Mat();
				Imgproc.GaussianBlur(channel, smooth, new Size(5.0, 5.0), 0.5);

				// Highgui.imwrite("/Users/narsir/Documents/Projects/Poolplayer/images/channel_"
				// + n + ".png", smooth);

				Imgproc.HoughCircles(smooth, circles,
						Imgproc.CV_HOUGH_GRADIENT, 1, 10, 350, 15, 5, 15);

				for (int i = 0; i < circles.cols(); i++) {
					double[] circ = circles.get(0, i);
					System.out.println(Arrays.toString(circ));
					houghCircles.add(new HoughCirle(circ));
				}
			}

			List<HoughCirle> toRemove = new ArrayList<Test.HoughCirle>();
			for (int i = 0; i < houghCircles.size(); i++) {
				for (int j = i + 1; j < houghCircles.size(); j++) {
					HoughCirle c1 = houghCircles.get(i);
					HoughCirle c2 = houghCircles.get(j);
					if (c1 == c2)
						continue;
					double dist = Math.sqrt((c1.x - c2.x) * (c1.x - c2.x)
							+ (c1.y - c2.y) * (c1.y - c2.y));
					if (dist < 15)
						toRemove.add(c1);
				}
			}

			houghCircles.removeAll(toRemove);

			for (HoughCirle c : houghCircles) {
				double totPix = 0;
				double numWhite = 0;
				double numBlack = 0;
				for (int i = (int) (c.x - c.r); i <= (int) (c.x + c.r); i++) {
					for (int j = (int) (c.y - c.r); j <= (int) (c.y + c.r); j++) {
						if (!(Math.sqrt((i - c.x) * (i - c.x) + (j - c.y)
								* (j - c.y)) <= c.r))
							continue;

						if (i < 0 || i >= src.width() || j < 0
								|| j >= src.height())
							continue;

						totPix++;

						double[] pixVals = src.get(j, i);
						if (pixVals == null)
							System.out.println(i + ", " + j);
						double maxval = pixVals[0] > pixVals[1] ? pixVals[0]
								: pixVals[1];
						maxval = maxval > pixVals[2] ? maxval : pixVals[2];

						double minval = pixVals[0] < pixVals[1] ? pixVals[0]
								: pixVals[1];
						minval = minval < pixVals[2] ? minval : pixVals[2];

						 if (pixVals[0] / maxval + pixVals[1] / maxval
						 + pixVals[2] / maxval > 2.5) {

//						if (maxval - minval < 20) {
							if (maxval < 150)
								numBlack++;
							else
								numWhite++;
						}

					}
				}

				if (numWhite / totPix < 0.3) {
					if (numBlack / totPix > 0.3)
						c.type = BallType.BLACK;
					else
						c.type = BallType.SOLID;
				} else {
					if (numWhite / totPix > 0.9)
						c.type = BallType.CUE;
					else
						c.type = BallType.STRIPE;
				}

				List<HoughCirle> cueballs = new ArrayList<Test.HoughCirle>();
				HoughCirle biggestCue = null;
				for (HoughCirle hc : houghCircles) {
					if (hc.type == BallType.CUE) {
						cueballs.add(hc);
						if (biggestCue == null)
							biggestCue = hc;
						else if (hc.r > biggestCue.r)
							biggestCue = hc;
					}
				}

				if (cueballs.size() > 1) {
					for (HoughCirle hc : cueballs) {
						if (hc != biggestCue)
							hc.type = BallType.SOLID;
					}
				}

				System.out.println(c.x + ", " + c.y + ": " + numWhite / totPix
						+ ", " + numBlack / totPix);
			}

			for (HoughCirle c : houghCircles) {
				System.out.println(c.x + ", " + c.y + ", " + c.r + " "
						+ c.type.name());
			}
			return src;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

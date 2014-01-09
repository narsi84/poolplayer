import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.sun.glass.ui.View.Capability;

public class Test extends Application {

	enum BallType {
		CUE, SOLID, STRIPE, BLACK
	}

	class HoughCirle {
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

	@Override
	public void start(Stage stage) {
		// load the image

			Image image;
			try {				
				image = new Image(
						new FileInputStream(
								"/Users/narsir/Documents/Projects/Poolplayer/OpenCVTest/images/rgb.png"));

				Mat src = Utils.Image2Mat(image);
				System.out.println(Arrays.toString(src.get(100, 100)));

				List<Mat> rgb = new ArrayList<Mat>();
				Core.split(src, rgb);

				Mat circles = new Mat();
				List<HoughCirle> houghCircles = new ArrayList<Test.HoughCirle>();

				for (int n = 0; n < 3; n++) {
					Mat channel = rgb.get(n);

					Mat smooth = new Mat();
					Imgproc.GaussianBlur(channel, smooth, new Size(5.0, 5.0),
							0.5);

					Imgproc.HoughCircles(smooth, circles,
							Imgproc.CV_HOUGH_GRADIENT, 1, 10, 350, 15, 5, 15);

					for (int i = 0; i < circles.cols(); i++) {
						double[] circ = circles.get(0, i);
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
							if (pixVals[0] / maxval + pixVals[1] / maxval
									+ pixVals[2] / maxval > 2.5) {
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

					System.out.println(c.x + ", " + c.y + ": " + numWhite
							/ totPix + ", " + numBlack / totPix);
				}

				Image edgeImage = Utils.Mat2Image(src);
				ImageView imgView = new ImageView(edgeImage);

				Group root = new Group();
				Scene scene = new Scene(root, src.width(), src.height());

				root.getChildren().add(imgView);

				for (HoughCirle hc : houghCircles) {
					Circle c = new Circle(hc.x, hc.y, hc.r);
					switch (hc.type) {
					case BLACK:
						c.setStroke(Color.WHITE);
						break;
					case CUE:
						c.setStroke(Color.BLACK);
						break;
					case SOLID:
						c.setStroke(Color.RED);
						break;
					case STRIPE:
						c.setStroke(Color.BLUE);
						break;
					}

					c.setFill(null);
					root.getChildren().add(c);
				}

				stage.setScene(scene);
				stage.setTitle("Gray");
				stage.show();

				Thread.sleep(3000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	public static void main(String[] args) {
		System.out.println(Math.atan2(1, -1));
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Application.launch(args);
	}

	public static void main2(String[] args) throws InterruptedException {
		System.out.println(Integer.toHexString(65535));

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture cap = new VideoCapture(0);

		// System.out.println(cap.get(Highgui.CV_CAP_PROP_FRAME_WIDTH));
		// System.out.println(cap.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));

		Mat frame = new Mat();

		Thread.sleep(2000);

		boolean succes = cap.read(frame);
		if (!succes) {
			// Cam closed or no video
			System.out.println("No cam");
		}

		Highgui.imwrite("test.jpg", frame);
		cap.release();

	}
}

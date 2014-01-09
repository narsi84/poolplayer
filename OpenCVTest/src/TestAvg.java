import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

public class TestAvg extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		Mat avgImg = Highgui
		.imread("/Users/narsir/Documents/Projects/Poolplayer/images/0.png");
		avgImg.convertTo(avgImg, CvType.CV_16UC3);
		
		System.out.println(CvType.typeToString(avgImg.type()));

		for (int i = 1; i < 10; i++) {
			Mat frame = Highgui
					.imread("/Users/narsir/Documents/Projects/Poolplayer/images/"
							+ i + ".png");
			frame.convertTo(frame, CvType.CV_16UC3);

			Core.add(avgImg, frame, avgImg);
		}

		Core.multiply(avgImg, new Scalar(1.0 / 10, 1.0/10, 1.0/10), avgImg);

		Image edgeImage = Utils.Mat2Image(avgImg);
		ImageView imgView = new ImageView(edgeImage);

		Group root = new Group();
		Scene scene = new Scene(root, avgImg.width(), avgImg.height());

		root.getChildren().add(imgView);
		stage.setScene(scene);
		stage.setTitle("Gray");
		stage.show();
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Application.launch(args);
	}
}

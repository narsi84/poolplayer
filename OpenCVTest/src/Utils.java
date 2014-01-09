import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Utils {

	public static Mat Image2Mat(Image image) {
		int height = (int) image.getHeight();
		int width = (int) image.getWidth();

		Mat mat = new Mat(height, width, CvType.CV_8UC3);
		PixelReader pixelReader = image.getPixelReader();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = pixelReader.getColor(x, y);
				mat.put(y, x, new double[] { color.getRed()*255.0, color.getGreen()*255.0,
						color.getBlue()*255.0 });
			}
		}
		return mat;
	}

	public static Image Mat2Image(Mat mat) {
		int height = mat.height();
		int width = mat.width();

		WritableImage writableImage = new WritableImage(width, height);
		PixelWriter pixelWriter = writableImage.getPixelWriter();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double[] pixVal = mat.get(j, i);
				if(pixVal.length == 1)
					pixelWriter.setColor(i, j, new Color(pixVal[0]/255.0, pixVal[0]/255.0, pixVal[0]/255.0, 1));
				else
					pixelWriter.setColor(i, j, new Color(pixVal[0]/255.0, pixVal[1]/255.0, pixVal[2]/255.0, 1));
			}
		}
		return writableImage;
	}
}

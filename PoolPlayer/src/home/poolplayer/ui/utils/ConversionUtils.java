package home.poolplayer.ui.utils;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ConversionUtils {

	public static ImageData convertMat2ImageData(Mat mat) {

		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
		
		Size size = mat.size();
		int numChannels = mat.channels();
		int depth = 8;
		switch (CvType.depth(mat.type())) {
		case CvType.CV_8U:
		case CvType.CV_8S:
			depth = 8;
			break;
		case CvType.CV_16U:
		case CvType.CV_16S:
			depth = 16;
			break;
		case CvType.CV_32S:
		case CvType.CV_32F:
			depth = 16;
			break;
		case CvType.CV_64F:
			depth = 64;
			break;
		}

		PaletteData palette;
		int mask = (1<<depth) - 1;
		if (numChannels == 1)
			palette = new PaletteData(mask, mask, mask);
		else if (numChannels == 3)
//			palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
			palette = new PaletteData( mask, mask << depth, mask << (2*depth));
		else {
			System.out.println("Invalid channel number");
			return null;
		}

		ImageData data = new ImageData((int) size.width, (int) size.height,
				numChannels * depth, palette);
		

		for (int x = 0; x < size.width; x++) {
			for (int y = 0; y < size.height; y++) {				
				double[] matVals = mat.get(y, x);				
				data.setPixel(x, y, palette.getPixel(new RGB((int)matVals[0], (int)matVals[1], (int)matVals[2])));
			}
		}
		return data;
	}
}

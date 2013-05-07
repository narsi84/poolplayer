package home.poolplayer.io;

import home.poolplayer.controller.Controller;
import home.poolplayer.imagecapture.ImageCapture;
import home.poolplayer.imageproc.AnalysisParams;
import home.poolplayer.imageproc.ImageProcessor;
import home.poolplayer.model.PoolTable;

import java.util.List;
import java.util.Properties;

public class SettingsReader extends ConfigFileReader {

	public static final String TABLE_SETTINGS = "Table";
	public static final String ANALYSIS_PARAMS = "Analysis";
	public static final String CAPTURE_SETTINGS = "Capture";

	public SettingsReader(String fname) {
		super(fname);
		parseSections();		
	}

	private void parseSections() {
		List<Section> sections = getSections();
		if (sections == null)
			return;
		
		for(Section sec : sections) {
			if (sec.getName().compareToIgnoreCase(TABLE_SETTINGS) == 0)
				parseTableSettings(sec);
			if (sec.getName().compareToIgnoreCase(ANALYSIS_PARAMS) == 0)
				parseAnalysisSettings(sec);
			if (sec.getName().compareToIgnoreCase(CAPTURE_SETTINGS) == 0)
				parseCaptureSettings(sec);
		}
	}
	
	private void parseCaptureSettings(Section section){
		ImageCapture capture = ImageCapture.getInstance();
		String val;
		
		Properties props = section.getProps();
		
		val = props.getProperty(SettingNames.FRAME_RATE.name());
		capture.setFrameRate(Integer.parseInt(val));
		
		val = props.getProperty(SettingNames.DEVICE_ID.name());
		capture.setDeviceId(Integer.parseInt(val));				
	}
	
	private void parseAnalysisSettings(Section section){
		AnalysisParams params = ImageProcessor.getInstance().getParams();
		String val;
		
		Properties props = section.getProps();
		
		val = props.getProperty(SettingNames.MIN_CIRC_DIST.name());
		params.setMinCircleDist(Double.parseDouble(val));
		
		val = props.getProperty(SettingNames.HOUGH_THR.name());
		params.setHoughThreshold(Double.parseDouble(val));
		
		val = props.getProperty(SettingNames.ACCUMULATOR_THR.name());
		params.setAccumulatorThreshold(Double.parseDouble(val));
		
		val = props.getProperty(SettingNames.MIN_RADIUS.name());
		params.setMinRadius(Integer.parseInt(val));
		
		val = props.getProperty(SettingNames.MAX_RADIUS.name());
		params.setMaxRadius(Integer.parseInt(val));
		
		val = props.getProperty(SettingNames.BLACK_LEVEL.name());
		params.setBlackLevel(Double.parseDouble(val));
		
		val = props.getProperty(SettingNames.COLOR_RATIO_THR.name());
		params.setColorRatioThreshold(Double.parseDouble(val));
		
		val = props.getProperty(SettingNames.CUE_PIXEL_RATIO.name());
		params.setCueBallPixelRatio(Double.parseDouble(val));
		
		val = props.getProperty(SettingNames.BLACK_PIXEL_RATIO.name());
		params.setBlackBallPixelRatio(Double.parseDouble(val));
		
		val = props.getProperty(SettingNames.WHITE_PIXEL_RATIO.name());
		params.setWhitePixelRatio(Double.parseDouble(val));		
	}

	private void parseTableSettings(Section section){
		PoolTable table = Controller.getInstance().getTable();

		Properties props = section.getProps();
		
		String val;
		val = props.getProperty(SettingNames.TABLE_X.name());
		table.setX(Integer.parseInt(val));

		val = props.getProperty(SettingNames.TABLE_Y.name());
		table.setY(Integer.parseInt(val));

		val = props.getProperty(SettingNames.TABLE_HEIGHT.name());
		table.setHeight(Integer.parseInt(val));

		val = props.getProperty(SettingNames.TABLE_WIDTH.name());
		table.setWidth(Integer.parseInt(val));

		val = props.getProperty(SettingNames.TABLE_FRICTION.name());
		table.setFriction(Double.parseDouble(val));

		val = props.getProperty(SettingNames.POCKET_RADIUS.name());
		table.setPocketRadius(Integer.parseInt(val));
		
		table.initPocketPositions();
	}
}

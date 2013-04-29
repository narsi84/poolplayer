package home.poolplayer.imageproc;

public class AnalysisParams {

	private double minCircleDist;
	private double houghThreshold;
	private double accumulatorThreshold;
	private int minRadius;
	private int maxRadius;

	private double blackLevel;
	private double colorRatioThreshold;
	private double cueBallPixelRatio;
	private double blackBallPixelRatio;
	private double whitePixelRatio;

	public AnalysisParams() {
		minCircleDist = 10;
		houghThreshold = 200;
		accumulatorThreshold = 15;
		minRadius = 15;
		maxRadius = 20;
		
		blackLevel = 150;
		colorRatioThreshold = 2.5;
		cueBallPixelRatio = 0.9;
		blackBallPixelRatio = 0.3;
		whitePixelRatio = 0.3;		
	}
	
	public double getMinCircleDist() {
		return minCircleDist;
	}

	public void setMinCircleDist(double minCircleDist) {
		this.minCircleDist = minCircleDist;
	}

	public double getHoughThreshold() {
		return houghThreshold;
	}

	public void setHoughThreshold(double houghThreshold) {
		this.houghThreshold = houghThreshold;
	}

	public double getAccumulatorThreshold() {
		return accumulatorThreshold;
	}

	public void setAccumulatorThreshold(double accumulatorThreshold) {
		this.accumulatorThreshold = accumulatorThreshold;
	}

	public int getMinRadius() {
		return minRadius;
	}

	public void setMinRadius(int minRadius) {
		this.minRadius = minRadius;
	}

	public int getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(int maxRadius) {
		this.maxRadius = maxRadius;
	}

	public double getBlackLevel() {
		return blackLevel;
	}

	public void setBlackLevel(double blackLevel) {
		this.blackLevel = blackLevel;
	}

	public double getColorRatioThreshold() {
		return colorRatioThreshold;
	}

	public void setColorRatioThreshold(double colorRatioThreshold) {
		this.colorRatioThreshold = colorRatioThreshold;
	}

	public double getCueBallPixelRatio() {
		return cueBallPixelRatio;
	}

	public void setCueBallPixelRatio(double cueBallPixelRatio) {
		this.cueBallPixelRatio = cueBallPixelRatio;
	}

	public double getBlackBallPixelRatio() {
		return blackBallPixelRatio;
	}

	public void setBlackBallPixelRatio(double blackBallPixelRatio) {
		this.blackBallPixelRatio = blackBallPixelRatio;
	}

	public double getWhitePixelRatio() {
		return whitePixelRatio;
	}

	public void setWhitePixelRatio(double whitePixelRatio) {
		this.whitePixelRatio = whitePixelRatio;
	}
}

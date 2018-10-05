package datamodel;

import java.awt.Rectangle;
import java.awt.Point;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {

	private Point origin;

	private String filePath;
	private VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;

	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds;

	public Video(String filePath) throws FileNotFoundException {
		origin = new Point();
		this.filePath = filePath;
		this.setVidCap(new VideoCapture(filePath));
		if (!getVidCap().isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		// fill in some reasonable default/starting values for several fields
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames() - 1;

		int frameWidth = (int) getVidCap().get(Videoio.CAP_PROP_FRAME_WIDTH);
		int frameHeight = (int) getVidCap().get(Videoio.CAP_PROP_FRAME_HEIGHT);
		this.arenaBounds = new Rectangle(0, 0, frameWidth, frameHeight);
	}

	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}

	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm) / 2;
	}

	public int getCurrentFrameNum() {
		return (int) getVidCap().get(Videoio.CV_CAP_PROP_POS_FRAMES);
	}

	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * @return frames per second
	 */
	public double getFrameRate() {
		return getVidCap().get(Videoio.CAP_PROP_FPS);
	}

	public Point getOrigin() {
		return origin;
	}

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public int getTotalNumFrames() {
		return (int) getVidCap().get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	public double getxPixelsPerCm() {
		return xPixelsPerCm;
	}

	public double getyPixelsPerCm() {
		return yPixelsPerCm;
	}

	public Mat readFrame() {
		Mat frame = new Mat();
		getVidCap().read(frame);
		return frame;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}

	public void setCurrentFrameNum(int seekFrame) {
		getVidCap().set(Videoio.CV_CAP_PROP_POS_FRAMES, (double) seekFrame);
	}

	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public void setOrigin(double x, double y) {
		origin.setLocation(x, y);
	}

	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public void setxPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	public void setyPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	public VideoCapture getVidCap() {
		return vidCap;
	}

	public void setVidCap(VideoCapture vidCap) {
		this.vidCap = vidCap;
	}

}

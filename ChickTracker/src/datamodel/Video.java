
package datamodel;

import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {

	
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private double frameRate;
	private int totalNumFrames;
	private String filePath;
	private int startFrameNum;
	private int endFrameNum;
	private Rectangle arenaBounds;

	private VideoCapture vidCap;

	/**
	 * Code from "SharedProjectCode" repository
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		
	
		
	}

	/**
	 * finds duration of video using frame rate and the total number of frames
	 * @return duration in seconds
	 */
	public double getDurationInSeconds() {
		return totalNumFrames/frameRate;

	}

	public String getVideoFileName() {
		return filePath;
		
	}

	

	public double getxPixelsPerCm() {
		return xPixelsPerCm;
	}

	public void setxPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	public double getyPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setyPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public void setFrameRate() {
		this.frameRate = vidCap.get(Videoio.CV_CAP_PROP_FPS);
		
	}

	public int getTotalNumFrames() {
		return totalNumFrames;
	}

	public void setTotalNumFrames() {
		this.totalNumFrames = (int) vidCap.get(Videoio.CV_CAP_PROP_FRAME_COUNT);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}

}

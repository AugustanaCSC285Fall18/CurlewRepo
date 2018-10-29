package datamodel;

import java.awt.Rectangle;
import java.awt.Point;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * The video object stores all the information for the entered .avi or .mp4
 * video file.
 * 
 * @author Team Curlew
 *
 */
public class Video {

	/**
	 * The Point origin is the reference for all measurements taken
	 */
	private Point origin;

	/**
	 * 
	 */
	private String filePath;
	private VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	private int videoStartFrameNum;
	private int videoEndFrameNum;

	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds;

	/**
	 * Constructs the video object
	 * 
	 * @param filePath The path to find the file
	 * @throws FileNotFoundException prevents crashing if the file isn't found
	 */
	public Video(String filePath) throws FileNotFoundException {
		origin = new Point();
		this.filePath = filePath;
		connectVideoCapture();
		// fill in some reasonable default/starting values for several fields
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames() - 1;
		this.videoStartFrameNum = 0;
		this.videoEndFrameNum = this.getTotalNumFrames() - 1;

		int frameWidth = (int) getVidCap().get(Videoio.CAP_PROP_FRAME_WIDTH);
		int frameHeight = (int) getVidCap().get(Videoio.CAP_PROP_FRAME_HEIGHT);
		this.arenaBounds = new Rectangle(0, 0, frameWidth, frameHeight);
	}

	// copied from ClassSharedRepo
	synchronized void connectVideoCapture() throws FileNotFoundException {
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}

	/**
	 * Converts frame numbers to seconds
	 * 
	 * @param numFrames
	 * @return frame number represented in seconds
	 */
	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}

	/**
	 * Converts seconds into frame number
	 * 
	 * @param numSecs
	 * @return frame number correlated to seconds entered
	 */
	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}

	/**
	 * Gets the arena bounds field
	 * 
	 * @return arenabounds as a Rectangle
	 */
	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	/**
	 * Calculates the average pixels per centimeter
	 * 
	 * @return calculated average number of pixels in a centimeter
	 */
	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm) / 2;
	}

	/**
	 * Gets the frame width from the video information
	 * 
	 * @return the width of the video being used
	 */
	public synchronized int getFrameWidth() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}

	/**
	 * Gets the frame height from the video information
	 * 
	 * @return the height of the video being used
	 */
	public synchronized int getFrameHeight() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}

	/**
	 * Gets the current frame of the video
	 * 
	 * @return the current frame number
	 */
	public int getCurrentFrameNum() {
		return (int) getVidCap().get(Videoio.CV_CAP_PROP_POS_FRAMES);
	}

	/**
	 * Gets the blank frame number
	 * 
	 * @return blank frame number
	 */
	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}

	/**
	 * Gets end of AutoTracking frame num
	 * 
	 * @return
	 */
	public int getEndAutoTrackFrameNum() {
		return endFrameNum;
	}

	/**
	 * Gets end of total video frame num (NOT autotrack)
	 * 
	 * @return int videoEndFrameNum
	 */
	public int getEndFrameNum() {
		return videoEndFrameNum;
	}

	/**
	 * Gets the file path as a string
	 * 
	 * @return file path string
	 */
	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * 
	 * @return frames per second
	 */
	public synchronized double getFrameRate() {
		return getVidCap().get(Videoio.CAP_PROP_FPS);
	}

	public Point getOrigin() {
		return origin;
	}

	/**
	 * Gets start of AutoTracking frame num
	 * 
	 * @return
	 */
	public int getStartAutroTrackFrameNum() {
		return startFrameNum;
	}

	/**
	 * Gets start of total video frame num (NOT autotrack)
	 * 
	 * @return videoStartFrameNum
	 */
	public int getStartFrameNum() {
		return videoStartFrameNum;
	}

	/**
	 * Gets the total number of frames in the video
	 * 
	 * @return int total number of frames
	 */
	public synchronized int getTotalNumFrames() {
		return (int) getVidCap().get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	/**
	 * Gets the horizontal pixels per centimeter
	 * 
	 * @return x pixels per cm scale
	 */
	public double getxPixelsPerCm() {
		return xPixelsPerCm;
	}

	/**
	 * Gets the vertical pixels per centimeter
	 * 
	 * @return y pixels per cm scale
	 */
	public double getyPixelsPerCm() {
		return yPixelsPerCm;
	}

	/**
	 * Accepts a Mat of a video frame and reads it
	 * 
	 * @return a Mat frame
	 */
	public synchronized Mat readFrame() {
		Mat frame = new Mat();
		getVidCap().read(frame);
		return frame;
	}

	/**
	 * Sets arena bounds for the video using a Rectangle parameter
	 * 
	 * @param arenaBounds Rectangle
	 */
	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}

	/**
	 * Sets the current frame of the video
	 * 
	 * @param int seekFrame, the frame number to be the current frame
	 */
	public synchronized void setCurrentFrameNum(int seekFrame) {
		getVidCap().set(Videoio.CV_CAP_PROP_POS_FRAMES, (double) seekFrame);
	}

	/**
	 * Sets the empty/blank frame of the video
	 * 
	 * @param int emptyFrameNum, the frame number to be the empty frame
	 */
	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}

	/**
	 * Sets the end of the autotracking by frame number
	 * 
	 * @param int endFrameNum, the end of the autotracking
	 */
	public void setEndAutoTrackFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	/**
	 * Sets the origin by accepting two doubles and creating a point object
	 * 
	 * @param x, x pixel value
	 * @param y, y pixel value
	 */
	public void setOrigin(double x, double y) {
		origin.setLocation(x, y);
	}

	/**
	 * Sets the start of the autotracking by frame number
	 * 
	 * @param int startFrameNum, the start of the autotracking
	 */
	public void setStartAutoTrackFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	/**
	 * Sets the number of pixels per centimeter horizontally
	 * 
	 * @param xPixelsPerCm, the double number of pixels per cm
	 */
	public void setxPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	/**
	 * Sets the number of pixels per centimeter vertically
	 * 
	 * @param yPixelsPerCm, the double number of pixels per cm
	 */
	public void setyPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	/**
	 * Gets the video capture being used for the video
	 * 
	 * @return vidcap being got
	 */
	public synchronized VideoCapture getVidCap() {
		return vidCap;
	}

	/**
	 * Sets the video capture being used for the video
	 * 
	 * @param vidCap Video Capture being set
	 */
	public synchronized void setVidCap(VideoCapture vidCap) {
		this.vidCap = vidCap;
	}

}

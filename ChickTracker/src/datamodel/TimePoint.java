package datamodel;

import java.util.List;

/**
 * The TimePoint object implements Comparable<TimePoint> and stores collected
 * information and specified time point/ video frame number
 * 
 * @author Team Curlew
 *
 */
public class TimePoint implements Comparable<TimePoint> {
	private double x; // location
	private double y;
	private int frameNum; // time (measured in frames)

	public TimePoint(double x, double y, int frameNum) {
		this.x = x;
		this.y = y;
		this.frameNum = frameNum;
	}

	/**
	 * Comparison based on the time (frame number).
	 */
	@Override
	public int compareTo(TimePoint other) {
		return this.getTimeDiffAfter(other);
	}

	@Override
	/**
	 * Equals method that tests to see if two TimePoints have the same location and
	 * the same time frame.
	 */
	public boolean equals(Object object) {
		if (!(object instanceof TimePoint)) {
			return false;
		} else {

			TimePoint other = (TimePoint) object;

			if (this.getTimeDiffAfter(other) == 0) {
				if (this.x == other.getX() && this.y == other.getY()) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Gets the distance between TimePoints
	 * 
	 * @param other, the other TimePoint
	 * @return the distance between the points as a double
	 */
	public double getDistanceTo(TimePoint other) {
		double dx = other.x - x;
		double dy = other.y - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Gets the frame number
	 * 
	 * @return int frame number
	 */
	public int getFrameNum() {
		return frameNum;
	}

	/**
	 * Gets a point and returns a java.awt.Point object
	 * 
	 * @return java.awt.Point object
	 */
	public java.awt.Point getPointAWT() {
		return new java.awt.Point((int) x, (int) y);
	}

	/**
	 * Gets a point as an org.opencv.core.Point object
	 * 
	 * @return org.opencv.core.Point object
	 */
	public org.opencv.core.Point getPointOpenCV() {
		return new org.opencv.core.Point(x, y);
	}

	/**
	 * How many frames have passed since another TimePoint
	 * 
	 * @param other - the otherTimePoint to compare with
	 * @return the difference (negative if the other TimePoint is later)
	 */
	public int getTimeDiffAfter(TimePoint other) {
		return this.frameNum - other.frameNum;
	}

	/**
	 * Gets x
	 * 
	 * @return double x
	 */
	public double getX() {
		return x;
	}

	/**
	 * Gets y
	 * 
	 * @return double y
	 */
	public double getY() {
		return y;
	}

	/**
	 * Sets x
	 * 
	 * @param x, the double value to be set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Sets y
	 * 
	 * @param y, the double value to be set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Gets the distance between to another point
	 * 
	 * @param x2, the double x value of the other point
	 * @param y2, the double y value of the other point
	 * @return the distance as a double value
	 */
	public double getDistanceTo(double x2, double y2) {
		double dx = x2 - x;
		double dy = y2 - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	/**
	 * Overrides the to String method to create a new to String method format
	 */
	public String toString() {
		return String.format("(%.1f,%.1f@T=%d)", x, y, frameNum);
	}

	/**
	 * Takes in an animal track and a frame number and checks which point in the
	 * animal track is closest to the given frame number.
	 * 
	 * @param animal, the AnimalTrack object in reference
	 * @param frameNum, the int object frame number
	 * @return The closest TimePoint object
	 */
	public static TimePoint closestPointToFrame(AnimalTrack animal, int frameNum) {
		int closestPoint = Integer.MAX_VALUE;
		TimePoint pointAtTime = null;
		List<TimePoint> closePoints = animal.getTimePointsWithinInterval(frameNum - 20, frameNum + 20);
		System.out.println(closePoints);
		for (TimePoint point : closePoints) {
			int frameDiff = Math.abs(frameNum - point.getFrameNum());
			if (frameDiff < closestPoint) {
				pointAtTime = point;
				closestPoint = point.getFrameNum();
			}
		}
		System.out.println(pointAtTime);
		return pointAtTime;
	}

}

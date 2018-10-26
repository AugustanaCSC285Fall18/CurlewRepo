package datamodel;

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
	 * Equals method that tests to see if two TimePoints have the same location and the same time frame.
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

	public double getDistanceTo(TimePoint other) {
		double dx = other.x - x;
		double dy = other.y - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public int getFrameNum() {
		return frameNum;
	}

	public java.awt.Point getPointAWT() {
		return new java.awt.Point((int) x, (int) y);
	}

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

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public double getDistanceTo(double x2, double y2) {
		double dx = x2 - x;
		double dy = y2 - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	
	@Override
	public String toString() {
		return String.format("(%.1f,%.1f@T=%d)", x, y, frameNum);
	}
}

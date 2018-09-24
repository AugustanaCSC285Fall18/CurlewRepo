package datamodel;
import java.awt.Point;

// some methods taken from Dr. Stonedahl's code

public class TimePoint {
	// data fields
	private int frameNum;
	private Point pt;
	
	public TimePoint(int x, int y, int frameNum) {
		pt = new Point(x,y);
		this.frameNum = frameNum;
	}
	
	public int getX() {
		return pt.x;
	}
	
	public int getY() {
		return pt.y;
	}
	
	public int getFrameNum() {
		return frameNum;
	}
	
	public Point getPt() {
		return pt;
	}
	
	@Override
	public String toString() {
		return "("+pt.x+","+pt.y+"@T="+frameNum +")";
	}

	public double getDistanceTo(TimePoint other) {
		return pt.distance(other.pt);
	}
	
}

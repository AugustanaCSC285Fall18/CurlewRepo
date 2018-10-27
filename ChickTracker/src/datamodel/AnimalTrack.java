package datamodel;

import java.util.ArrayList;
import java.util.List;

import datamodel.TimePoint;

public class AnimalTrack {
	private String animalID;

	private List<TimePoint> positions;

	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}

	public void add(TimePoint pt) {
		int frameNum = pt.getFrameNum();
		for (int index = 0; index < positions.size(); index++) {
			if (positions.get(index).getFrameNum() == frameNum) {
				System.out.println("point in frame " + positions.get(index).getFrameNum() + " removed");
				this.removePoint(positions.get(index));
			}
		}
		positions.add(pt);
	}

	/**
	 * Adds the positions of two AnimalTracks to this AnimalTrack
	 * @param other - AnimalTrack to be added
	 */
	public void add(AnimalTrack other) {
		List<TimePoint> otherPoints = other.positions;
		for (TimePoint point : otherPoints) {
			positions.add(point);
		}
	}
	
	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}

	/**
	 * Returns the TimePoint at the specified time, or null
	 * 
	 * @param frameNum
	 * @return
	 */

	public TimePoint getTimePointAtTime(int frameNum) {

        int counter = 0, size = positions.size() - 1;
        
        while (counter <= size) 
        { 
            int m = counter + (size-counter)/2; 
  
            // Check if frameNum is present at mid 
            if (positions.get(m).getFrameNum() == frameNum) 
                return positions.get(m); 
  
            // If frameNum is greater, ignore left half 
            if (positions.get(m).getFrameNum() < frameNum) 
                counter = m + 1; 
  
            // If frameNum is smaller, ignore right half 
            else
                size = m - 1; 
        } 
  
        // if we reach here, then frameNum was  
        // not present 
        return null; 	
	}
	
	/**
	 * 
	 * @param startFrameNum - the starting time (inclusive)
	 * @param endFrameNum   - the ending time (inclusive)
	 * @return all time points in that time interval
	 */
	public List<TimePoint> getTimePointsWithinInterval(int startFrameNum, int endFrameNum) {
		List<TimePoint> pointsInInterval = new ArrayList<>();
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() >= startFrameNum && pt.getFrameNum() <= endFrameNum) {
				pointsInInterval.add(pt);
			}
		}
		return pointsInInterval;
	}


	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size() - 1);
	}

	public String getId() {
		return animalID;
	}

	public String toString() {
		int startFrame = 0;
		int endFrame = 0;
		if (!positions.isEmpty()) {
			startFrame = positions.get(0).getFrameNum();
			endFrame = getFinalTimePoint().getFrameNum();
		}
		return "AnimalTrack[id=" + animalID + ",numPts=" + positions.size() + " start=" + startFrame + " end="
				+ endFrame + "]";
	}

	public int getNumPoints() {
		return positions.size();
	}
	
	public void removePoint(TimePoint point) {
		positions.remove(point);
	}
}
